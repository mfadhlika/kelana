import { axiosInstance, handleResponse } from "@/lib/request";
import type { Response } from "@/types/response";
import type { Stats } from "@/types/stats";

class StatsService {
    fetchStats = async (): Promise<Response<Stats>> => {
        return await handleResponse(axiosInstance.get<Response<Stats>>(`v1/stats`));
    }
}

export const statsService = new StatsService();
