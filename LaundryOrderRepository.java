package com.laundrymgmt.modern.repository;

import com.laundrymgmt.modern.model.LaundryOrder;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LaundryOrderRepository extends JpaRepository<LaundryOrder, Long> {

    List<LaundryOrder> findAllByOrderByOrderDateDescIdDesc();

    List<LaundryOrder> findAllByCustomer_IdOrderByOrderDateDescIdDesc(Long customerId);

    Optional<LaundryOrder> findByOrderCodeIgnoreCase(String orderCode);

    long countByOrderDate(LocalDate orderDate);

    @Query("select coalesce(sum(o.bill), 0) from LaundryOrder o")
    BigDecimal sumAllRevenue();

    @Query("select coalesce(sum(o.bill), 0) from LaundryOrder o where o.orderDate = :orderDate")
    BigDecimal sumRevenueByDate(@Param("orderDate") LocalDate orderDate);

    @Query("select o.orderDate, count(o) from LaundryOrder o group by o.orderDate order by o.orderDate asc")
    List<Object[]> countOrdersByDate();

    @Query("select o.orderDate, coalesce(sum(o.bill), 0) from LaundryOrder o group by o.orderDate order by o.orderDate asc")
    List<Object[]> sumRevenueGroupedByDate();

    List<LaundryOrder> findTop5ByOrderByOrderDateDescIdDesc();
}
