# CLAYJOE Functional Document

## 1. Purpose and Business Context

CLAYJOE provides a small independent handmade ceramics business with a branded website for presenting products and managing purchases. Customers can discover products, maintain a shopping cart, place orders, simulate payment and follow an order through delivery confirmation. Administrators maintain the catalogue and record shipments.

This document describes user-visible functions and the business rules implemented in the current version. It is a functional baseline, rather than a claim that every scenario has passed end-to-end testing. Architecture, class diagrams, entity relationships, database exports and installation instructions belong to the other delivery documents.

The application serves one merchant. It does not provide merchant onboarding, independent seller stores or marketplace settlement. Catalogue images were supplied locally, and prices and stock are demonstration data rather than verified live inventory or prices from an external store.

## 2. Users and Access

| Capability | Visitor | Signed-in customer | Administrator |
| --- | --- | --- | --- |
| Browse published products, categories and details | Yes | Yes | Yes |
| Switch interface language and select a shipping market | Yes | Yes | Yes |
| Register a customer account | Yes | Available through the registration page | Does not create administrator privileges |
| Use a personal cart and place orders | Sign-in required | Yes | Available as an authenticated account |
| View, pay for, cancel or complete own eligible orders | Sign-in required | Yes | Available for own eligible orders |
| Create/edit products and change publication status | No | No | Yes |
| View store-wide orders and record shipments | No | No | Yes |

Customers cannot access another customer's cart or orders. Administrator privileges are assigned outside public registration; there is no user-facing role-management screen.

## 3. Functional Catalogue

### 3.1 Storefront and Product Discovery

| ID | Function | User-visible behaviour and rules |
| --- | --- | --- |
| F01 | Store homepage | Presents CLAYJOE branding, product imagery, navigation and the published product collection. |
| F02 | Product listing | Shows product names, images, descriptions, prices and available stock information. Unpublished products are excluded from the public catalogue. |
| F03 | Category filtering | Customers can choose All, Cups, Figurines or Tableware. A category with no matching published products displays an empty-state message. |
| F04 | Product details | Opens a product-specific page containing its image, description, price and stock information. Products with a configured gallery provide additional images. Unavailable or unpublished product links do not expose a purchasable public detail page. |
| F05 | Add to cart entry | A signed-in user can add an available product from its detail page. Unauthenticated users receive a sign-in prompt. Invalid price, insufficient stock or an unpublished product can prevent the operation. |

### 3.2 Accounts and Authentication

| ID | Function | User-visible behaviour and rules |
| --- | --- | --- |
| F06 | Customer registration | Accepts a username, password and password confirmation. Successful registration opens the login page. Duplicate or invalid information produces feedback. Registration creates a customer account. |
| F07 | Sign in | Valid credentials establish a signed-in session. Navigation reflects the current account and its permissions. Invalid credentials are rejected. |
| F08 | Sign out | Ends the session and updates the navigation. Subsequent protected operations require sign-in. Expired sessions also produce sign-in feedback. |

Registration rules:

- Usernames contain 3–50 ASCII letters, digits or underscores. Usernames are normalised to lowercase and must be unique.
- Passwords contain 12–72 characters, cannot consist only of whitespace, and must not exceed 72 bytes when UTF-8 encoded.
- Password confirmation must match before the form is submitted.
- Password reset, email verification and social sign-in are outside the current scope.

### 3.3 Shopping Cart

| ID | Function | User-visible behaviour and rules |
| --- | --- | --- |
| F09 | Persistent personal cart | Displays the current account's cart with product names, images, unit prices, quantities, subtotals and stock/availability information. Cart data is associated with the account and can be retrieved after a later sign-in. |
| F10 | Adjust quantity | Supports increasing and decreasing an item's quantity. Quantities must be positive whole numbers and must satisfy current availability and stock checks. Adding the same product again increases its existing cart quantity. |
| F11 | Remove and refresh | Removes a selected item or reloads the current cart. An empty cart displays a message and a route back to shopping. |
| F12 | Cart price summary | Shows item subtotals and an estimated total in the selected market’s settlement currency. Missing valid quotes prevent checkout. Displayed availability may change before checkout. |

Adding a product to the cart does not reserve or deduct stock. The final stock and price checks occur when an order is placed.

### 3.4 Checkout and Customer Orders

| ID | Function | User-visible behaviour and rules |
| --- | --- | --- |
| F13 | Enter shipping details | Collects a shipping country/region, recipient name, phone number and full shipping address. All are required. The country/region must match the selected market (CN or SG). Length limits are 100, 30 and 500 characters respectively. Users are prompted to include country, city, street, unit number and postal code. The application does not verify an address with a postal service. |
| F14 | Place an order | Validates the cart and current product data, calculates the order amount, creates a pending-payment order, deducts available stock and clears the checked-out cart. Failure must not leave a partially completed checkout. |
| F15 | Protect against duplicate checkout | Repeated submission of the same checkout request returns the existing result rather than creating another order. After an uncertain network result, the user is directed to check My orders. Retrying retains the original request identity, market and currency. Changing interface language permits a retry; changing market/currency while the result is uncertain is blocked. |
| F16 | My orders and order details | Shows the customer's own orders, their statuses, totals, currencies and creation times. Details include purchased items, quantities, snapshot unit prices, shipping information, saved market and shipping country/region, and recorded shipment information. Earlier orders without recorded region values display an explicit fallback rather than an inferred region. |
| F17 | Cancel an unpaid order | Allows the owner to cancel a pending-payment order. Cancellation restores its stock once. Already cancelled requests do not restore stock again. Paid, shipped and completed orders cannot use this cancellation operation. |
| F18 | Confirm receipt | Allows the owner to complete a shipped order. The order becomes Completed. Repeating completion does not create another transaction. |

The current total covers product line items. The application does not calculate separate shipping charges, tax charges, discounts or coupons.

### 3.5 Demonstration Payments

| ID | Function | User-visible behaviour and rules |
| --- | --- | --- |
| F19 | Select a simulated payment method | Offers WeChat Pay, Alipay, PayPal and PayNow as demonstration choices for an eligible pending-payment order. |
| F20 | Simulate success or failure | Success changes the order to Paid. Failure leaves it Awaiting payment so another payment attempt can be made. The amount and currency come from the saved order. Each attempt is recorded; repeated identical requests return the recorded result. |

**Payment boundary:** These choices do not connect to the named payment providers, open a real payment session or transfer money. The simulation is available only when the backend's local demonstration payment capability is enabled. Refunds and real payment-provider callbacks are not implemented.

### 3.6 Administrator Product Management

| ID | Function | User-visible behaviour and rules |
| --- | --- | --- |
| F21 | Manage the catalogue | Lists published and unpublished products, their identifiers and publication statuses. Provides refresh, add, edit and publish/unpublish actions. |
| F22 | Create a product | Accepts Chinese name and description, English name and description, base price, stock, category and main image URL. A successful creation opens its product detail page. New products use SGD as the base currency. |
| F23 | Edit a product | Loads the existing values, permits changes and returns to product management after saving. The form identifies the product's existing base currency. Changes do not rewrite historical order amounts or item snapshots. |
| F24 | Publish or unpublish | Controls whether the product is publicly available. Unpublished products remain visible to administrators and remain available as historical order references. This is not permanent deletion. |

Product input rules:

| Field | Rule |
| --- | --- |
| Chinese name | Required; not only whitespace; at most 255 characters |
| English name | Optional; at most 255 characters |
| Chinese / English description | Optional; at most 2,000 characters each |
| Base price | Required; 0.01–99,999,999.99; up to two decimal places |
| Stock | Required; a non-negative whole number within the application's supported integer range |
| Category | Cups, Figurines or Tableware |
| Main image URL | Optional; at most 500 characters; references an existing image |

The image field is not an upload facility. Additional detail-gallery images currently require catalogue configuration. Administrators do not create new category types through the interface.

### 3.7 Administrator Order Management

| ID | Function | User-visible behaviour and rules |
| --- | --- | --- |
| F25 | View store-wide orders | Shows orders across customers, including line items, amount/currency, status, recipient information, saved market and shipping country/region, and any recorded shipment details. |
| F26 | Record a shipment | For a Paid order, accepts a carrier and tracking number, each required and at most 100 characters. The administrator confirms the action. The order changes to Shipped, and shipment information and time are recorded. |
| F27 | Refresh order information | Reloads current order states so the administrator can check changes or resolve uncertainty after an operation. An already shipped/completed order cannot be overwritten through a conflicting repeated shipment request. |

Shipment information is entered manually. The application does not obtain live delivery events from a logistics provider.

### 3.8 Markets, Language, Currency and Shared Feedback

| ID | Function | User-visible behaviour and rules |
| --- | --- | --- |
| F28 | Switch language | A single Language menu offers Simplified Chinese and English. The selection applies to customer pages and the four administrator pages: product management, product creation, product editing and order management. The browser retains the selection where local storage is available. Language selection is independent of the shipping market and does not change settlement currency or monetary amounts. |
| F29 | Display bilingual product content | Uses the stored name and description for the selected language. Administrators edit the two language versions separately. Stored order item names are also selected by language. Missing translations may use the original name or a fallback description; the application does not automatically translate product data. |
| F30 | Display and settle in two currencies | The Mainland China market (CN) uses CNY and the Singapore market (SG) uses SGD, regardless of interface language. The fixed demonstration rate defaults to 1 SGD = 5.30 CNY and is identified as non-live. Product and cart prices are calculated by the backend. |
| F31 | Preserve historical amounts | An order retains the unit prices, total and currency selected at checkout. Changing language, the current market, catalogue prices or the configured demonstration rate does not convert an existing order. Its recorded market and shipping country/region also remain unchanged. Payment attempts inherit the order's saved amount and currency. |
| F32 | Provide operation feedback | Displays loading, empty, validation, success, permission, unavailable-resource and network-failure feedback. Relevant controls are disabled during operations to reduce accidental repeated actions. Interface messages, including administrator form feedback and shipment confirmation, support both languages. |
| F33 | Select a shipping market | A separate market selector offers Mainland China (CN) and Singapore (SG). Both support Chinese and English. The choice controls settlement currency, is remembered where browser storage is available, and must match the declared shipping country/region. |

Market and currency rules:

- This is one application with regional shopping settings, shared accounts, catalogue and stock; it is not two independently deployed platforms.
- Customers select Mainland China (CN) or Singapore (SG). Where local storage is available, the choice survives refresh. Without a selection, regional prices are unavailable and checkout is blocked.
- Both markets support both interface languages. The backend derives settlement currency from the market and rejects mismatched submitted currencies or shipping countries/regions.
- The country/region check validates the declared selection, not the physical address. There is no automatic address verification or geolocation-based eligibility enforcement.
- Original product prices remain in their stored base currency; switching language or market does not edit catalogue data.
- Converted unit prices are rounded to two decimal places before multiplication by quantity.
- Example: SGD 10.05 at the demonstration rate becomes CNY 53.27 per item; three items total CNY 159.81.
- Checkout uses current server-side prices. The cart total is an estimate until the order is created.
- The demonstration rate can be changed through application configuration; there is no administrator exchange-rate editor or live exchange-rate feed.
- Shipping names, addresses, usernames and other user-entered text are retained as entered. They are not automatically translated when interface language changes.

### 3.9 Personal Message Centre

Authenticated customers and administrators can view their own notifications at `/notifications`. Administrators do not gain access to every customer's inbox. New order creation, successful simulated payment and shipment each trigger a bilingual notification for the order owner. The page shows the latest 20 notifications, newest first, with title, content, local creation time, read/unread status and an order link when applicable. Refresh retrieves the latest data. Switching language updates titles and content. Empty, signed-out and service-error states have distinct feedback.

Viewing a message does not mark it read; no mark-as-read endpoint is implemented. Historical operations are not backfilled. Cancellation and receipt confirmation do not currently generate messages. These are in-app notifications, not email or SMS. Delivery is best effort after the commerce transaction commits; failures are logged without undoing orders, and there is no automatic retry.

## 4. Principal User Workflows

### 4.1 Customer Purchase and Delivery

1. Browse the collection and filter by category if required.
2. Open a product, review its details and availability, and sign in.
3. Add products to the cart and adjust quantities.
4. Select the shipping market and, independently, the preferred interface language. Enter a matching shipping country/region and the required delivery details.
5. Submit the order and review the pending-payment result.
6. Select a demonstration payment method and simulate success.
7. After the administrator records shipment, review the carrier and tracking number.
8. Confirm receipt to complete the order.

**Outcome:** A completed order retains the purchased items, shipping details and original transaction amounts.

### 4.2 Cancellation Before Payment

1. Open an owned pending-payment order.
2. Request cancellation and confirm where prompted.
3. Review the Cancelled state.

**Outcome:** Available stock is restored once. Repeating cancellation does not increase stock again.

### 4.3 Product Administration

1. Sign in with administrator permissions and open Product management.
2. Create or edit product information, including both language versions where available.
3. Correct any field errors and save.
4. Set the appropriate publication status.
5. Review the public listing and details to confirm the customer-facing content.

**Outcome:** The catalogue reflects the saved content and availability; existing order snapshots remain unchanged.

### 4.4 Shipment Administration

1. Open Order management and identify a Paid order.
2. Review the saved market, shipping country/region, recipient and delivery address.
3. Enter the carrier and tracking number.
4. Confirm shipment and review the updated order.

**Outcome:** The order becomes Shipped and its delivery details become visible to its owner.

## 5. Order Lifecycle and Operational Rules

| Current state | Permitted action | Result |
| --- | --- | --- |
| Awaiting payment (`PENDING_PAYMENT`) | Simulated payment succeeds | Paid (`PAID`) |
| Awaiting payment | Simulated payment fails | Remains Awaiting payment |
| Awaiting payment | Owner cancels | Cancelled (`CANCELLED`); stock restored once |
| Paid | Administrator records shipment | Shipped (`SHIPPED`) |
| Shipped | Owner confirms receipt | Completed (`COMPLETED`) |
| Cancelled or Completed | View historical record | No new purchase or payment transition |

Invalid transitions are rejected by the backend, even if a request is submitted without using the normal page controls. Pending orders do not currently expire automatically; manual cancellation is the implemented route for releasing their deducted stock.

## 6. Exceptions and Recovery

| Situation | Expected behaviour |
| --- | --- |
| Missing/invalid form values | Show feedback and do not accept an invalid operation. |
| Duplicate registration username | Reject registration and prompt the user to choose another username. |
| Not signed in or session expired | Require sign-in before protected actions. |
| Customer accesses administrator operations | Deny access. |
| User requests another customer's order/cart record | Do not reveal or modify the other customer's record. |
| Product missing, unpublished, unavailable or short of stock | Prevent the relevant purchase operation and display feedback. |
| Checkout fails during processing | Do not leave a partially saved order with inconsistent stock/cart updates. |
| Checkout response is uncertain | Ask the customer to inspect My orders before retrying; retain the original request identity. A retry in a different currency is not treated as a new checkout. |
| Shipment result is uncertain | Ask the administrator to refresh and inspect the recorded order state. |
| Catalogue price, language or selected market changes after purchase | Keep the existing order’s original monetary values and saved region fields. |
| Shipping country/region or submitted currency conflicts with the market | Reject checkout before database access in the business service; do not create an order. |
| Earlier order has no saved region | Display “Region not recorded for this earlier order”; do not infer it from currency. |

## 7. Functional Acceptance Checklist

This checklist defines demonstration scenarios. It is not a record that all scenarios have already been executed successfully.

| Check | Functions | Expected result |
| --- | --- | --- |
| Browse and filter published products | F01–F04 | Correct listing, category results and product details; unpublished products excluded. |
| Register, sign in and sign out | F06–F08 | Valid account flow works; invalid input and duplicates produce feedback. |
| Add, adjust and remove cart items | F05, F09–F12 | Correct quantities and subtotals; current account's data only. |
| Place a valid order | F13–F15 | Pending order created, current prices saved, stock reduced and cart cleared. |
| Retry the same checkout | F15 | Same order returned; no second stock deduction. |
| Cancel an unpaid order twice | F17 | One cancellation and one stock restoration. |
| Simulate successful and failed payment | F19–F20 | Success marks Paid; failure preserves Awaiting payment; no real charge. |
| Record shipment and confirm receipt | F18, F25–F27 | Paid → Shipped → Completed; shipment information visible. |
| Create, edit and unpublish a product | F21–F24 | Saved data reflected in management; correct public visibility. |
| Switch language on customer and administrator pages | F28–F29, F32 | Interface text changes and available translated product content is selected. |
| Check CNY/SGD pricing and historical orders | F30–F31, F33 | Quotes follow the market and fixed rate; language changes leave amounts unchanged; saved orders retain their checkout currency, amount and regions. |
| Check market persistence and shipping restrictions | F13–F15, F33 | Market survives refresh where storage is available; a mismatched shipping region is rejected with localised feedback. |
| Attempt unauthorised access | All protected functions | Customer data and administrator operations remain protected. |

### 7.1 Regional Shopping Verification — 23 October 2026

The learner reported that all 9 backend tests in `PricingServiceTest` and all 6 frontend tests across `pricing.service.spec.ts` and `cart.spec.ts` passed. Source inspection confirmed the corresponding tests and region fields. The document update did not independently rerun those tests.

The learner also confirmed completion of the four-part browser acceptance round: (1) the CN/SG × Chinese/English selection matrix and refresh persistence, (2) shipping-region mismatch rejection with bilingual feedback, (3) historical order amounts and recorded regions remaining unchanged, and (4) matching region and amount information in customer and administrator order views. This is learner-reported acceptance, not a newly captured automated browser run.

The focused tests use mocked dependencies or HTTP responses. They do not establish real payment integration, physical address validity, concurrency behaviour under load or a complete regression pass for every function above.

## 8. Current Scope Boundaries

The following are not implemented user functions in this version:

- Real payment-provider integration, financial refunds or merchant payout settlement.
- Multiple merchants, seller onboarding, commissions or separate seller administration.
- Live exchange rates or an in-app exchange-rate editor.
- Independently deployed regional platforms, separate regional stock pools, physical address verification or automatic enforcement of geographic eligibility.
- Automatic AI translation or content generation.
- Image-file uploading or arbitrary gallery management through the administrator interface.
- Keyword search, pagination, product variants or user-defined categories.
- Customer address books, password recovery, email verification, email or SMS delivery.
- Live logistics tracking, automated unpaid-order expiry, return/refund workflows.
- Shipping-fee, tax, coupon or discount calculation.

The current application runs as a local demonstration. Public production hosting is not established by this document.
