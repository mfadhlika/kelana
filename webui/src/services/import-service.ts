import { axiosInstance, handleResponse } from "@/lib/request";
import type { Import } from "@/types/import";
import type { Import as ImportRequest } from "@/types/requests/import";
import type { Response } from "@/types/response";

class ImportService {
    createImport = async (im: ImportRequest): Promise<Response> => {
        const formData = new FormData();
        formData.set("source", im.source);
        formData.set("file", im.file.item(0)!);

        return handleResponse(axiosInstance.post(`v1/imports`, formData, {
            headers: {
                "Content-Type": "multipart/form-data"
            }
        }));
    }

    fetchImports = async (): Promise<Response<Import[]>> => {
        return axiosInstance
            .get<Response<Import[]>>(`v1/imports`)
            .then(res => res.data);
    }

    fetchImportRawContent = async (id: number): Promise<Blob> => {
        return handleResponse(axiosInstance.get(`v1/imports/${id}/raw`, {
            responseType: 'blob'
        }));
    }

    deleteImport = async (id: number): Promise<Response> => {
        return handleResponse(axiosInstance.delete(`v1/imports/${id}`));
    }
}

export const importService = new ImportService();
