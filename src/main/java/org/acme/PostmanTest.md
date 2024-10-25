# Postman Test

## UserResource Postman Test

1. **Create a new user**
    - **Request**
        - Method: POST
        - URL: http://localhost:8080/users
        - Headers:
            - Content-Type: application/json
        - Body:
            ```json
            {
                "username": "John Doe",
                "email": "john@example.com",
                "password": "password123",
                "role": "customer"
            }
            ```
    - **Expected Response**
        - Status: 201 Created
        - Response Body:
          ```json
          {
              "id": 1,
              "name": "John Doe",
              "email": "john@example.com",
              "password": "password123",
              "role": "customer"
          }
          ```
2. **Get all users**
    - **Request**
        - Method: GET
        - URL: http://localhost:8080/users
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
                  "id": 1,
                  "username": "stored username",
                  "email": "stored email",
                  "password": "stored password",
                  "role": "selected role"
          }
          ```
3. **Get user by id**
    - **Request**
        - Method: GET
        - URL: http://localhost:8080/users/1
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
              "id": 1,
              "username": "stored username",
              "email": "stored email",
              "password": "stored password",
              "role": "selected role"
          }
          ```
4. **Update user by ID**
    - **Request**
        - Method: PUT
        - URL: http://localhost:8080/users/1
        - Headers:
            - Content-Type: application/json
        - Body:
            ```json
            {
                "username": "update username",
                "email": "update email",
                "password": "update password"
            }
            ```
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
              "id": 1,
              "username": "updated username",
              "email": "updated email",
              "password": "stored password",
              "role": "selected role"
          }
          ```
5. **Delete user by ID**
    - **Request**
        - Method: DELETE
        - URL: http://localhost:8080/users/1
    - **Expected Response**
        - Status: 204 No Content

## ProductResource Postman Test

1. **Create a new Product**
    - **Request**
        - Method: POST
        - URL: http://localhost:8080/products
        - Headers:
            - Content-Type: application/json
        - Body:
            ```json
            {
                "productName": "Laptop",
                "description": "High-end gaming laptop",
                "price": 1500.00,
                "imageURL": "http://example.com/laptop.jpg"
            }
            ```
    - **Expected Response**
        - Status: 201 Created
        - Response Body:
          ```json
          {
              "id": 1,
              "productName": "Laptop",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
          ```
2. **Get all products**
    - **Request**
        - Method: GET
        - URL: http://localhost:8080/products
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
              "id": 1,
              "productName": "Laptop",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
          /*rest of the products*/
          ```
3. **Get Product by id**
    - **Request**
        - Method: GET
        - URL: http://localhost:8080/products/1
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
              "id": 1,
              "productName": "Laptop",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
          ```
4. **Update Product by ID**
    - **Request**
        - Method: PUT
        - URL: http://localhost:8080/products/1
        - Headers:
            - Content-Type: application/json
        - Body:
            ```json
          {
              "id": 1,
              "productName": "LaptopUpdate",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
            ```
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
              "id": 1,
              "productName": "LaptopUpdate",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
          ```
5. **Delete user by ID**
    - **Request**
        - Method: DELETE
        - URL: http://localhost:8080/products/1
    - **Expected Response**
        - Status: 204 No Content

## OrderResource Postman Test

1. **Create a new Order Guest and User**
    - **Request**
        - Method: POST
        - URL: http://localhost:8080/orders
        - Headers:
            - Content-Type: application/json
        - Body (User Order):
            ```json
            {
                "totalAmount": 100.5,
                "status": "pending",
                "user": {
                    "id": 1
                } 
            }
            ```
        - Body (Guest Order):
            ```json
            {
                "totalAmount": "Laptop",
                "status": "pending"
            }
            ```
    - **Expected Response**
        - Status: 201 Created
        - Response Body (User Order):
          ```json
          {
              "message": "Your order id number is [orderId]"
          }
          ```
        - Response Body (Guest Order):
          ```json
          {
              "message": "Your order guest tracking number is [guestTrackingId]"
          }
          ```
2. **Get all Orders**
    - **Request**
        - Method: GET
        - URL: http://localhost:8080/orders
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
              "id": 1,
              "totalAmount": 100.5,
              "orderDate": "2024-10-20T12:34:56",
              "status": "pending",
              "user": {
                    "id": 1,
                    "username": "stored username",
                    "email": "stored email",
                    "password": "stored password",
                    "role": "selected role"
              }
          }

          ```
3. **Get Order by id**
    - **Request**
        - Method: GET
        - URL: http://localhost:8080/orders/1
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
              "id": 1,
              "productName": "Laptop",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
          ```

4. **Get Order by guestTrackingId**
    - **Request**
        - Method: GET
        - URL: http://localhost:8080/orders/1
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
              "id": 1,
              "productName": "Laptop",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
          ```

5. **Update Product by ID**
    - **Request**
        - Method: PUT
        - URL: http://localhost:8080/orders/1
        - Headers:
            - Content-Type: application/json
        - Body:
            ```json
          {
              "id": 1,
              "productName": "LaptopUpdate",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
            ```
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
              "id": 1,
              "productName": "LaptopUpdate",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
          ```
6. **Update Product by guestTrackingId**
    - **Request**
        - Method: PUT
        - URL: http://localhost:8080/orders/1
        - Headers:
            - Content-Type: application/json
        - Body:
            ```json
          {
              "id": 1,
              "productName": "LaptopUpdate",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
            ```
    - **Expected Response**
        - Status: 200 OK
        - Response Body:
          ```json
          {
              "id": 1,
              "productName": "LaptopUpdate",
              "description": "High-end gaming laptop",
              "price": 1500.00,
              "imageURL": "http://example.com/laptop.jpg"
          }
          ```          

7. **Delete user by ID**
    - **Request**
        - Method: DELETE
        - URL: http://localhost:8080/orders/1
    - **Expected Response**
        - Status: 204 No Content
