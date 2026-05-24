package org.dsoft.boundary;

import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.dsoft.entity.dto.UserAdminDTO;
import org.dsoft.service.UserManagementService;

@Path("/api/admin/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
@RolesAllowed({"ADMIN"})
public class UserManagementController {

    @Inject
    UserManagementService userManagementService;

    @GET
    public Response getAllUsers(
        @QueryParam("search") String searchQuery,
        @QueryParam("role") String roleFilter
    ) {
        try {
            List<UserAdminDTO> users = userManagementService.getAllUsers(searchQuery, roleFilter);
            return Response.ok(users).build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error fetching users: " + e.getMessage())
                .build();
        }
    }

    @GET
    @Path("/{userId}")
    public Response getUserDetails(@PathParam("userId") Long userId) {
        try {
            UserAdminDTO user = userManagementService.getUserDetails(userId);
            return Response.ok(user).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error fetching user: " + e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{userId}/role")
    public Response updateUserRole(
        @PathParam("userId") Long userId,
        @QueryParam("newRole") String newRole
    ) {
        try {
            if (newRole == null || newRole.isEmpty()) {
                return Response.status(Response.Status.BAD_REQUEST)
                    .entity("newRole parameter is required")
                    .build();
            }

            UserAdminDTO user = userManagementService.updateUserRole(userId, newRole);
            return Response.ok(user).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error updating user role: " + e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{userId}/deactivate")
    public Response deactivateUser(@PathParam("userId") Long userId) {
        try {
            UserAdminDTO user = userManagementService.deactivateUser(userId);
            return Response.ok(user).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error deactivating user: " + e.getMessage())
                .build();
        }
    }

    @PUT
    @Path("/{userId}/activate")
    public Response reactivateUser(@PathParam("userId") Long userId) {
        try {
            UserAdminDTO user = userManagementService.reactivateUser(userId);
            return Response.ok(user).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error reactivating user: " + e.getMessage())
                .build();
        }
    }

    @DELETE
    @Path("/{userId}")
    public Response deleteUser(@PathParam("userId") Long userId) {
        try {
            userManagementService.deleteUser(userId);
            return Response.ok("User deleted").build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.NOT_FOUND)
                .entity(e.getMessage())
                .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity("Error deleting user: " + e.getMessage())
                .build();
        }
    }
}
