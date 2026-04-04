package com.laundrymgmt.modern.repository;

import com.laundrymgmt.modern.model.Role;
import com.laundrymgmt.modern.model.UserAccount;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {

    Optional<UserAccount> findByCustomerCodeIgnoreCase(String customerCode);

    boolean existsByPhone(String phone);

    long countByRole(Role role);

    List<UserAccount> findTop5ByRoleOrderBySignupDateDesc(Role role);

    @Query("select u.signupDate, count(u) from UserAccount u where u.role = :role group by u.signupDate order by u.signupDate asc")
    List<Object[]> countSignupsByDate(@Param("role") Role role);

    boolean existsByCustomerCodeIgnoreCase(String customerCode);

    long countBySignupDateAndRole(LocalDate signupDate, Role role);
}
