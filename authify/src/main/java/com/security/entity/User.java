package com.security.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String userId;

    private String name;

    private String password;

    private String gender;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "user_images",
            joinColumns = @JoinColumn(name = "user_id") // FK to your main entity
    )
    @Column(name = "image_url")
    private List<String> images = new ArrayList<>();


    @Column(unique = true)
    private String email;

    private String verifyOtp;

    private Boolean isAccountVerifiedAt;

    private LocalDateTime verifyOtpExpiredAt;

    private String resetOtp;

    private LocalDateTime resetOtpExpiredAt;

    // feel free to add more fields ad required

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(name = "user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roleEntities ;

    @CreationTimestamp
    @Column(name = "CREATE_DATE", updatable = false)
    private LocalDateTime createDateTime;

    @UpdateTimestamp
    @Column(name = "UPDATE_DATE", insertable = false)
    private LocalDateTime updateDateTime;
}
