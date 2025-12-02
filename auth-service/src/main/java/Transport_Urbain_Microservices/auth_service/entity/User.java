package Transport_Urbain_Microservices.auth_service.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String username;

    private String password;  // Hashed

    @Column(unique = true)
    private String email;

    @ElementCollection
    private List<String> roles;  // e.g., ["USER", "ADMIN"]

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Getters, setters, constructors...
}