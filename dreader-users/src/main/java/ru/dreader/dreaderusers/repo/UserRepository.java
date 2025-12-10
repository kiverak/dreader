package ru.dreader.dreaderusers.repo;

//import org.springframework.stereotype.Repository;
//
//import java.util.Optional;
//import java.util.UUID;
//
//@Repository
//public interface UserRepository extends JpaRepository<User, String> {
//
//    Optional<User> findByEmail(String email);
//
//    void deleteByEmail(String email);
//
//    @Query("""
//            SELECT u FROM User u where
//            (:email is null or :email='' or lower(u.email) like lower(concat('%', :email,'%')))
//            or
//            (:username is null or :username='' or lower(u.username) like lower(concat('%', :username,'%')))
//            """)
//    Page<User> findByParams(@Param("email") String email,
//                            @Param("username") String username,
//                            Pageable pageable);
//}
