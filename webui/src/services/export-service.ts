import { axiosInstance, handleResponse } from "@/lib/request";
import type { Export } from "@/types/export";
import type { Export as ExportRequest } from "@/types/requests/export";
import type { Response } from "@/types/response";

class ExportService {
    createExport = async (ex: ExportRequest): Promise<Response> => {
        return handleResponse(axiosInstance.post<Response>(`v1/exports`, ex));
    }

    fetchExports = async (): Promise<Response<Export[]>> => {
        return handleResponse(axiosInstance
            .get<Response<Export[]>>(`v1/exports`));
    }

    fetchExportRawContent = async (id: number): Promise<Blob> => {
        return handleResponse(axiosInstance.get(`v1/exports/${id}/raw`, {
            responseType: 'blob'
        }));
    }

    deleteExport = async (id: number): Promise<Response> => {
        return handleResponse(axiosInstance.delete(`v1/exports/${id}`));
    }
}

export const exportService = new ExportService();
