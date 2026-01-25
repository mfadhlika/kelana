import { axiosInstance } from "@/lib/request";
import type { Response } from "@/types/response";
import type { Integration } from "@/types/integration";

class IntegrationService {
    fetchIntegration = async (): Promise<Response<Integration>> => {
        return await axiosInstance.get<Response<Integration>>("v1/integration").then(res => res.data);
    }

    submitIntegration = async (data: Integration): Promise<Response<Integration>> => {
        return await axiosInstance.put<Response<Integration>>("v1/integration", data).then(res => res.data);
    }

    sendOwntracksCommand = async (action: string): Promise<void> => axiosInstance.post(`v1/integration/owntracks/cmd/${action}`);
}

export const integrationService: IntegrationService = new IntegrationService();
