package com.quickbite.cart.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cart_items")
@Getter
@Setter
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;

    @Column(name = "menu_item_id", nullable = false)
    private Long menuItemId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "qty", nullable = false)
    private int qty;

    @Column(name = "unit_price_cents", nullable = false)
    private long unitPriceCents;

    public CartItem(Long menuItemId, String name, int qty, long unitPriceCents) {
        this.menuItemId = menuItemId;
        this.name = name;
        this.qty = qty;
        this.unitPriceCents = unitPriceCents;
    }

    public long lineTotalCents() {
        return unitPriceCents * qty;
    }
}
