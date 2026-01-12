package com.fadhlika.lokasi.controller.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fadhlika.lokasi.dto.Response;
import com.fadhlika.lokasi.service.BackupService;

@RestController
@RequestMapping("/api/v1/backups")
public class BackupController {
    @Autowired
    private BackupService backupService;

    @PostMapping
    public Response<Void> createBackup() {
        backupService.createBackup();

        return new Response<>("success");
    }
}
