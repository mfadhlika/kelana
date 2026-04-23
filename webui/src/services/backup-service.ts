import { axiosInstance, handleResponse } from "@/lib/request";

class BackupService {
    createBackup = async (): Promise<void> => handleResponse(axiosInstance.post("v1/backups"));
}

export const backupService: BackupService = new BackupService();
