package com.votechori.backend.repository;

import com.votechori.backend.model.ElectionSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ElectionSettingRepository extends JpaRepository<ElectionSetting, Long> {
}
