import { axiosInstance } from "@/lib/request";

class BackupService {
    createBackup = async (): Promise<void> => axiosInstance.post("v1/backups");
}

export const backupService: BackupService = new BackupService();
