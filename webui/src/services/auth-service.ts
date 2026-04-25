import { axiosInstance, handleResponse } from "@/lib/request";
import type { Login as LoginResponse } from "@/types/responses/login";
import type { Login as LoginRequest } from "@/types/requests/login";
import type { Response } from "@/types/response";

class AuthService {
    login = async (req: LoginRequest): Promise<Response<LoginResponse>> => {
        return await handleResponse(axiosInstance.post<Response<LoginResponse>>("v1/login", {
            username: req.username,
            password: req.password
        }));
    }

    validate = async (): Promise<Response> => {
        return await handleResponse(axiosInstance.get<Response>("v1/auth/validate"));
    }

    logout = async (): Promise<Response> => handleResponse(axiosInstance.delete("v1/logout"));
}

export const authService: AuthService = new AuthService();
