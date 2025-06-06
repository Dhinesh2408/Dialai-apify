	package com.example.Diallock_AI.Repository;
	
	import java.util.Optional;
	
	import org.springframework.data.jpa.repository.JpaRepository;
	import org.springframework.stereotype.Repository;
	
	import com.example.Diallock_AI.model.Setting;
	
	
	@Repository
	public interface SettingRepo extends JpaRepository<Setting, Integer>{
		Optional<Setting> findByUserId(int userId);
	
	}
