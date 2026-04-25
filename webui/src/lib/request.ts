import axios, { type AxiosResponse } from "axios";
import { toast } from "sonner";
import type { Login } from "@/types/responses/login";
import type { Response } from "@/types/response";
import router from "./router";
import { authService } from "@/services/auth-service";
import { useAuthStore } from "@/hooks/use-auth";
import { AuthenticationError } from "@/types/errors";

let refreshTokenPromise: Promise<string> | null = null;
let logoutPromise: Promise<Response> | null = null;

const axiosInstance = axios.create({
    baseURL: "/api",
    withCredentials: true,
    headers: {
        "Content-Type": "application/json"
    }
});

async function logout(): Promise<Response> {
    try {
        const res = await authService.logout();
        useAuthStore.getState().logout();

        await router.navigate("/login", {
            replace: true
        });
        return res;
    } catch (err) {
        console.error(err);
        toast.error(`logging out failed: ${err}`);
        throw err;
    }
};

async function refreshToken(): Promise<string> {
    const { data } = await axiosInstance.get<Response<Login>>("v1/auth/refresh");
    useAuthStore.getState().login(data.data.accessToken);
    return data.data.accessToken;
};

axiosInstance.interceptors.request.use(
    (req) => {
        const accessToken = localStorage.getItem("accessToken");
        if (accessToken) req.headers.Authorization = "Bearer " + accessToken;
        return req;
    },
    (err) => {
        return Promise.reject(err);
    }
);

axiosInstance.interceptors.response.use(
    (res) => res,
    async (err) => {
        const originalRequest = err.config;

        if (!/^(v1\/auth\/refresh|v1\/login)$/.test(originalRequest.url) &&
            err.response?.status == 401 &&
            !originalRequest._retry) {
            originalRequest._retry = true;

            if (!refreshTokenPromise) {
                console.debug("refreshing token");
                refreshTokenPromise = refreshToken();
            }

            try {
                const accessToken = await refreshTokenPromise;

                if (accessToken) originalRequest.headers.Authorization = "Bearer " + accessToken;

                console.debug("retry request");
                return axios(originalRequest);
            } catch (err) {
                if (!logoutPromise)
                    logoutPromise = logout();

                console.error("failed refreshing token", err);
                const res = await logoutPromise;
                return Promise.reject(new AuthenticationError(res.message));
            } finally {
                refreshTokenPromise = null;
                logoutPromise = null;
            }
        }

        return Promise.reject(err);
    }
);

const handleResponse = async <T>(promis: Promise<AxiosResponse<T>>): Promise<T> => {
    try {
        const res = await promis;
        return res.data;
    } catch (err) {
        if (axios.isAxiosError(err)) {
            throw err.response?.data
        }
        throw err;
    }
}

export { axiosInstance, handleResponse };
