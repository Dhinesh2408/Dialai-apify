package com.example.Diallock_AI.Service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Diallock_AI.Repository.SettingRepo;
import com.example.Diallock_AI.model.Setting;
import com.example.Diallock_AI.model.User;


@Service
public class SettingService {

    @Autowired
    private SettingRepo repo;

    public Setting saveSetting(Setting setting, User user) {
        Optional<Setting> existingSetting = repo.findByUserId(user.getId());
        if (existingSetting.isPresent()) {
            Setting existing = existingSetting.get();
            setting.setId(existing.getId()); // Ensure the ID is set for update
        }
        setting.setUser(user);
        
        return repo.save(setting);
    }

    public Optional<Setting> getSettingByUserId(int userId) {
        return repo.findByUserId(userId);
    }
}

