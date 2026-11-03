package com.keerthana.bank.model;
import jakarta.persistence.*;
import java.time.LocalDateTime;
@Entity @Table(name="bank_users")
public class BankUser {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true,length=60) private String username;
 @Column(nullable=false,unique=true,length=140) private String email;
 @Column(nullable=false) private String passwordHash;
 @Column(nullable=false) private LocalDateTime createdAt=LocalDateTime.now();
 protected BankUser(){}
 public BankUser(String username,String email,String passwordHash){this.username=username;this.email=email;this.passwordHash=passwordHash;}
 public Long getId(){return id;} public String getUsername(){return username;} public String getEmail(){return email;} public String getPasswordHash(){return passwordHash;} public LocalDateTime getCreatedAt(){return createdAt;}
}
