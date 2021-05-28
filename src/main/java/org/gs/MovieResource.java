package org.gs;

import org.jboss.logging.Logger;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.List;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.NOT_FOUND;

@Path("/movies")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class MovieResource {

    private Logger LOGGER = Logger.getLogger(MovieResource.class);

    @Inject MovieRepository movieRepository;

    @GET
    public Response getAll() {
        LOGGER.debug("Get all movies inside the database");
        List<Movie> movies = movieRepository.listAll();
        return Response.ok(movies).build();
    }

    @GET
    @Path("{id}")
    public Response getById(@PathParam("id") Long id) {
        LOGGER.debug("Get the movie with id " + id);
        return movieRepository
                .findByIdOptional(id)
                .map(movie -> Response.ok(movie).build())
                .orElse(Response.status(NOT_FOUND).build());
    }

    @GET
    @Path("title/{title}")
    public Response getByTitle(@PathParam("title") String title) {
        LOGGER.debug("Get the movie with title " + title);
        return movieRepository
                .find("title", title)
                .singleResultOptional()
                .map(movie -> Response.ok(movie).build())
                .orElse(Response.status(NOT_FOUND).build());
    }

    @GET
    @Path("country/{country}")
    public Response getByCountry(@PathParam("country") String country) {
        LOGGER.debug("Get the movie with country " + country);
        List<Movie> movies = movieRepository.findByCountry(country);
        return Response.ok(movies).build();
    }

    @POST
    @Transactional
    public Response create(Movie movie) {
        LOGGER.debug("Saving new movie inside the database");
        movieRepository.persist(movie);
        if (movieRepository.isPersistent(movie)) {
            LOGGER.info("The movie has been saved with id "+ movie.getId());
            return Response.created(URI.create("/movies/" + movie.getId())).build();
        }
        LOGGER.error("The movie has not been saved inside the database");
        return Response.status(NOT_FOUND).build();
    }

    @DELETE
    @Path("{id}")
    @Transactional
    public Response deleteById(@PathParam("id") Long id) {
        LOGGER.debug("Deleting movie with id " + id);
        boolean deleted = movieRepository.deleteById(id);
        if(deleted) {
            LOGGER.info("The movie has been deleted");
            return Response.noContent().build();
        }
        LOGGER.error("The movie has not been deleted");
        return Response.status(BAD_REQUEST).build();
    }
}