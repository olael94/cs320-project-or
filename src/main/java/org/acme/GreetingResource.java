package org.acme;

import jakarta.transaction.Transactional;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import java.util.List;

import org.acme.UserName;

//This defines the path of the resource
@Path("/hello")
public class GreetingResource {

    @GET// this is a GET HTTP request
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello RESTEasy";
    }

    @Path("/personalized/{name}")
    @POST// this is a GET HTTP request
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    public String personalizedHello(@PathParam("name") String name) {
        UserName userName = new UserName(name);
        userName.persist();
        return "Hello " + name + "! Your name has been stored in the database. ";
    }

    @POST// this is a GET HTTP request
    @Path("/personalized")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String personalizedHelloPost(Person p) {
        return "Hello " + p.getFirstName() + " " + p.getLastName();
    }

    //CREATE: Add a new user
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    //The createUser method takes a UserName object as input and persists (adds) it to the database.
    public String createUser(UserName userName) {
        userName.persist();
        return "User created " + userName;
    }

    //READ: Get all users
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<UserName> getAllUsers() {
        return UserName.listAll();
    }

    // UPDATE: Update a user's name by ID
    @PUT
    @Path("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    @Transactional
    //The updateUser method takes a UserName object as input and updates the user with the specified ID.
    public String updateUser(@PathParam("id") Long id, UserName updatedUser) {
        UserName user = UserName.findById(id);
        if (user == null) {
            throw new WebApplicationException("User with id " + id + " does not exist.", 404);
        }
        user.name = updatedUser.name;
        return "User updated to: " + user.name;
    }

    // DELETE: Delete a user by ID
    @DELETE
    @Path("/{id}")
    @Transactional
    public void deleteUser(@PathParam("id") Long id) {
        UserName user = UserName.findById(id);
        if (user == null) {
            throw new WebApplicationException("User with id " + id + " does not exist.", 404);
        }
        user.delete();
    }


    public static class Person {
        private String firstName;
        private String lastName;

        public String getFirstName(){
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName(){
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

}

