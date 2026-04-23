import { axiosInstance, handleResponse } from "@/lib/request";
import type { Response } from "@/types/response";
import type { Integration } from "@/types/integration";

class IntegrationService {
    fetchIntegration = async (): Promise<Response<Integration>> => {
        return handleResponse(axiosInstance.get<Response<Integration>>("v1/integration"));
    }

    submitIntegration = async (data: Integration): Promise<Response<Integration>> => {
        return await handleResponse(axiosInstance.put<Response<Integration>>("v1/integration", data));
    }

    sendOwntracksCommand = async (action: string): Promise<void> => handleResponse(axiosInstance.post(`v1/integration/owntracks/cmd/${action}`));
}

export const integrationService: IntegrationService = new IntegrationService();
