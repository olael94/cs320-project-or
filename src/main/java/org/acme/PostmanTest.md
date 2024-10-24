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
4. **Update user**
    - **Request**
      - Method: PUT
        - URL: http://localhost:8080/users/1
        - Headers:
            - Content-Type: application/json
        - Body:
            ```json
            {
                "username": "Jane Doe",
                "email": "
                "password": "password123",
            }
            ```