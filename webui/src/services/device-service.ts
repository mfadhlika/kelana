import { axiosInstance, handleResponse } from "@/lib/request";
import type { Response } from "@/types/response";

class DeciceService {
    fetchDevices = async (): Promise<Response<string[]>> => {
        return await handleResponse(axiosInstance.get<Response<string[]>>("v1/user/devices"));
    }
}

export const deviceService: DeciceService = new DeciceService();
