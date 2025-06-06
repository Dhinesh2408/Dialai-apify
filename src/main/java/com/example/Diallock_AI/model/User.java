package com.example.Diallock_AI.model;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "users")
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(unique = true, nullable = false)
    private String email;

    private String password;  // No @JsonIgnore here

    private String name;

    private String phone;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<DiallockModel> leads;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Setting setting;
}
