namespace java com.dr0l3.thrift.generated
#@namespace scala com.dr0l3.thrift.generated

struct User {
    1: string id;
    2: string name;
    3: optional UserPreferences preferences
}

enum PaymentMethod {
    CREDIT_CARD,
    BANK_TRANSFER
}

struct UserPreferences {
    1: string favoriteColor
    2: PaymentMethod paymentMethod
}

struct Order {
    1: list<string> items
    2: double totalPrice
}

struct UserResponse {
    1: optional User user
}

struct UserPreferenceResponse {
    1: optional UserPreferences preference
}

struct OrderResponse {
    1: list<Order> orders
}

service UserService {
    UserResponse getUserById(1: string id)
    UserResponse createUser(1: string name)
}

service UserPreferenceService {
    UserPreferenceResponse getPreferencesForUser(1: string id)
    UserPreferenceResponse updatePreferenceForUser(1: string id, 2: UserPreferences preferences)
}


service OrderService {
    OrderResponse getOrdersForUser(1: string id)
    OrderResponse addOrderToUser(1: string id, 2: Order order)
}
