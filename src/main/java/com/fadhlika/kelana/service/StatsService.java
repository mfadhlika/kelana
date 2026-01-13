package com.fadhlika.kelana.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fadhlika.kelana.dto.Stats;
import com.fadhlika.kelana.repository.StatsRepository;

@Service
public class StatsService {
    @Autowired
    private StatsRepository statsRepository;

    public Stats getUserStats(int userId) {
        return statsRepository.getStats(userId);
    }
}
