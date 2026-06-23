# QuickBite — Dependency Graphs

Mermaid diagrams derived directly from [`PLATFORM_SPEC.md`](../PLATFORM_SPEC.md). Edges are kept
faithful to the spec: §1.1 for sync dependencies, §5 for events, §1.2 for MFE→service calls.

---

## (a) Service-to-service synchronous dependencies (spec §1.1)

Arrows point from caller to callee (`A --> B` means "A makes Feign `/internal` calls to B").

```mermaid
graph LR
  identity["identity-service :8081"]
  restaurant["restaurant-service :8082"]
  menu["menu-service :8083"]
  search["search-service :8084"]
  cart["cart-service :8085"]
  pricing["pricing-service :8086"]
  promotion["promotion-service :8087"]
  order["order-service :8088"]
  payment["payment-service :8089"]
  wallet["wallet-service :8090"]
  driver["driver-service :8091"]
  delivery["delivery-service :8092"]
  notification["notification-service :8093"]
  review["review-service :8094"]

  restaurant --> identity
  menu --> restaurant
  search --> restaurant
  search --> menu
  cart --> menu
  cart --> identity
  promotion --> identity
  pricing --> menu
  pricing --> promotion
  order --> cart
  order --> pricing
  order --> menu
  order --> restaurant
  order --> identity
  wallet --> identity
  payment --> order
  payment --> wallet
  payment --> identity
  driver --> identity
  delivery --> order
  delivery --> driver
  review --> order
  review --> restaurant
  review --> identity
```

> `notification-service` has no synchronous Feign edges in the steady state — it learns about
> order/delivery/payment activity from events (see diagram b). `identity-service` is the
> foundational leaf; `order-service` and `delivery-service` are the high-fan-in hubs.

---

## (b) Kafka event flows (spec §5)

```mermaid
flowchart LR
  subgraph Producers
    restaurantP["restaurant-service"]
    menuP["menu-service"]
    orderP["order-service"]
    paymentP["payment-service"]
    deliveryP["delivery-service"]
  end

  subgraph Topics
    tRestaurant(["restaurant.events"])
    tMenu(["menu.events"])
    tOrders(["orders.events"])
    tPayments(["payments.events"])
    tDeliveries(["deliveries.events"])
  end

  subgraph Consumers
    searchC["search-service"]
    notificationC["notification-service"]
    orderC["order-service"]
    deliveryC["delivery-service"]
  end

  restaurantP -->|RestaurantUpserted / RestaurantStatusChanged| tRestaurant
  menuP -->|MenuItemUpserted| tMenu
  orderP -->|OrderCreated / OrderStatusChanged| tOrders
  paymentP -->|PaymentCaptured / PaymentFailed / Refunded| tPayments
  deliveryP -->|DeliveryStatusChanged| tDeliveries

  tRestaurant --> searchC
  tMenu --> searchC

  tOrders --> notificationC
  tOrders --> deliveryC

  tPayments --> notificationC
  tPayments --> orderC

  tDeliveries --> notificationC
  tDeliveries --> orderC
```

---

## (c) MFE → service calls (through the gateway, spec §1.2)

Every MFE calls services through `api-gateway` (`VITE_GATEWAY_URL`, default `http://localhost:8080`).

```mermaid
graph LR
  shell["shell :3000"]
  discovery["discovery-mfe :3001"]
  checkout["checkout-mfe :3002"]
  tracking["order-tracking-mfe :3003"]
  account["account-mfe :3004"]
  admin["restaurant-admin-mfe :3005"]
  driverportal["driver-portal-mfe :3006"]

  gateway["api-gateway :8080"]

  identity["identity-service"]
  restaurant["restaurant-service"]
  menu["menu-service"]
  search["search-service"]
  cart["cart-service"]
  pricing["pricing-service"]
  promotion["promotion-service"]
  order["order-service"]
  payment["payment-service"]
  wallet["wallet-service"]
  driver["driver-service"]
  delivery["delivery-service"]
  review["review-service"]

  shell --> gateway
  discovery --> gateway
  checkout --> gateway
  tracking --> gateway
  account --> gateway
  admin --> gateway
  driverportal --> gateway

  gateway -.-> identity
  gateway -.-> restaurant
  gateway -.-> menu
  gateway -.-> search
  gateway -.-> cart
  gateway -.-> pricing
  gateway -.-> promotion
  gateway -.-> order
  gateway -.-> payment
  gateway -.-> wallet
  gateway -.-> driver
  gateway -.-> delivery
  gateway -.-> review

  shell ==>|auth| identity

  discovery ==> search
  discovery ==> restaurant
  discovery ==> menu

  checkout ==> cart
  checkout ==> pricing
  checkout ==> promotion
  checkout ==> order
  checkout ==> payment
  checkout ==> wallet

  tracking ==> order
  tracking ==> delivery

  account ==> identity
  account ==> wallet
  account ==> review
  account ==> order

  admin ==> restaurant
  admin ==> menu
  admin ==> order
  admin ==> promotion

  driverportal ==> driver
  driverportal ==> delivery
```

> Solid bold edges (`==>`) show the logical "talks to" target per spec §1.2; all traffic physically
> flows MFE → `api-gateway` (`-->`) → service (`-.->`).
