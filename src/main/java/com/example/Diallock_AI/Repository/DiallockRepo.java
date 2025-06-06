package com.example.Diallock_AI.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.Diallock_AI.model.DiallockModel;



@Repository
public interface DiallockRepo extends JpaRepository<DiallockModel, Integer> {

    

    @Query("SELECT p FROM DiallockModel p WHERE p.user.id = :userId AND ("
            + "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.phoneno) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.companysize) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.country) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.status) LIKE LOWER(CONCAT('%', :keyword, '%')) OR "
            + "LOWER(p.company) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<DiallockModel> findByKeyword(@Param("keyword") String keyword, @Param("userId") int userId);

    @Query("SELECT p FROM DiallockModel p WHERE p.user.id = :userId AND "
            + "LOWER(p.status) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<DiallockModel> findStatus(@Param("keyword") String keyword, @Param("userId") int userId);

    List<DiallockModel> findByUser_Id(int userId); // Corrected method name
}