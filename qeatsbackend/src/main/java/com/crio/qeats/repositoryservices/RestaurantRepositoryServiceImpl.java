/*
 *
 *  * Copyright (c) Crio.Do 2019. All rights reserved
 *
 */

// package com.crio.qeats.repositoryservices;

// import com.crio.qeats.configs.RedisConfiguration;
// // import ch.hsr.geohash.GeoHash;
// import com.crio.qeats.dto.Restaurant;
// import com.crio.qeats.globals.GlobalConstants;
// // import com.crio.qeats.globals.GlobalConstants;
// import com.crio.qeats.models.RestaurantEntity;
// import com.crio.qeats.repositories.RestaurantRepository;
// import com.crio.qeats.utils.GeoLocation;
// // import com.crio.qeats.utils.GeoLocation;
// import com.crio.qeats.utils.GeoUtils;
// import ch.hsr.geohash.GeoHash;
// import com.fasterxml.jackson.core.JsonProcessingException;
// import com.fasterxml.jackson.core.type.TypeReference;
// // import com.fasterxml.jackson.core.JsonProcessingException;
// // import com.fasterxml.jackson.core.type.TypeReference;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import java.io.IOException;
// // import java.io.IOException;
// import java.time.LocalTime;
// import java.util.ArrayList;
// // import java.util.Arrays;
// // import java.util.HashSet;
// import java.util.List;
// // import java.util.Optional;
// // import java.util.Set;
// // import java.util.concurrent.Future;
// // import java.util.regex.Pattern;
// // import java.util.stream.Collectors;
// import javax.inject.Provider;
// import org.modelmapper.ModelMapper;
// import org.springframework.beans.factory.annotation.Autowired;
// // import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Jedis;
// import org.springframework.context.annotation.Primary;
// // import org.springframework.data.mongodb.core.MongoTemplate;
// // import org.springframework.data.mongodb.core.query.Criteria;
// // import org.springframework.data.mongodb.core.query.Query;
// // import org.springframework.data.mongodb.repository.MongoRepository;
// // import org.springframework.scheduling.annotation.AsyncResult;
// import org.springframework.stereotype.Service;
// import redis.clients.jedis.Jedis;

// @Primary
// @Service
// public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {

//   @Autowired
//   RedisConfiguration redisConfiguration;

//   // @Autowired
//   // private MongoTemplate mongoTemplate;

//   @Autowired
//   private Provider<ModelMapper> modelMapperProvider;

//   @Autowired
//   RestaurantRepository restaurantRepository;


//   private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
//     LocalTime openingTime = LocalTime.parse(res.getOpensAt());
//     LocalTime closingTime = LocalTime.parse(res.getClosesAt());
    
//     return time.isAfter(openingTime) && time.isBefore(closingTime);
//   }

//   // : CRIO_TASK_MODULE_NOSQL
//   // Objectives:
//   // 1. Implement findAllRestaurantsCloseby.
//   // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
//   // Check RestaurantRepositoryService.java file for the interface contract.
//   public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
//       Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

//     ModelMapper modelMapper = modelMapperProvider.get();
//     List<Restaurant> restaurants = new ArrayList<>();

//     // Database call start time
//     // long startTimeInMillis = System.currentTimeMillis();

//     // Call to repository to find restaurants
//     // List<RestaurantEntity> restaurantList = restaurantRepository.findAll();
//     if (redisConfiguration.isCacheAvailable()) {
//       restaurants = findAllRestaurantsCloseByFromCache(latitude, longitude, currentTime, servingRadiusInKms);
//     } 
//     else {
//       restaurants = findAllRestaurantsCloseFromDb(latitude, longitude, currentTime, servingRadiusInKms);
//     }


//     // Database call end time
//     // long endTimeInMillis = System.currentTimeMillis();

//     // System.out.println("Database call took :" + (endTimeInMillis - startTimeInMillis)); 

//     /* Debugging statment to check no of restaurants returned from repository
//     * System.out.println("No of restaurants returned from database : " + restaurantList.size());
//     */
      
//     // if (restaurantsList == null) {
//     //   System.out.println("Repository returning empty list");
//     // }
//     // else {
//     //   for(RestaurantEntity restaurant : restaurantsList) {
//     //       if(isRestaurantCloseByAndOpen(restaurant, currentTime, latitude, longitude, servingRadiusInKms)) {
//     //       restaurants.add(modelMapper.map(restaurant, Restaurant.class));
//     //     }         
//     //   } 
//     // }
  
//     return restaurants;
//   }

//   private List<Restaurant> findAllRestaurantsCloseByFromCache(Double latitude, Double longitude,
//       LocalTime currentTime, Double servingRadiusInKms) {

//         List<Restaurant> restaurantList = new ArrayList<>();
//         GeoLocation geoLocation = new GeoLocation(latitude, longitude);
//         GeoHash geoHash = GeoHash.withCharacterPrecision(geoLocation.getLatitude(), 
//           geoLocation.getLongitude(), 7);

//           Jedis jedis = redisConfiguration.getJedisPool().getResource();
//           String jsonStringFromCache = jedis.get(geoHash.toBase32());

//           if(jsonStringFromCache == null) {
//             //Cache needs to be updated
//             String createdJsonString = "";
//             try {
//               restaurantList = findAllRestaurantsCloseFromDb(geoLocation.getLatitude(), 
//                 geoLocation.getLongitude(), currentTime, servingRadiusInKms);
              
//               // Converting the "restaurantList" object to JSON
//               createdJsonString = new ObjectMapper().writeValueAsString(restaurantList);
//             }
//             catch (JsonProcessingException e) {
//               e.printStackTrace();
//             }

//             // Do operation with jedis resource
//             jedis.setex(geoHash.toBase32(), 
//               GlobalConstants.REDIS_ENTRY_EXPIRY_IN_SECONDS, createdJsonString);
//           }
//           else {
//             try {
//               restaurantList = new ObjectMapper().readValue(jsonStringFromCache, 
//                 new TypeReference<List<Restaurant>>(){});
//             }
//             catch (IOException e) {
//               e.printStackTrace();
//             }
//           }      

//       return restaurantList;
//     }
  

//   private List<Restaurant> findAllRestaurantsCloseFromDb(Double latitude, Double longitude,
//       LocalTime currentTime, Double servingRadiusInKms) {

//         ModelMapper modelMapper = modelMapperProvider.get();
//         List<RestaurantEntity> restaurantEntities = restaurantRepository.findAll();
//         List<Restaurant> restaurants = new ArrayList<Restaurant>();
//         for (RestaurantEntity restaurantEntity : restaurantEntities) {
//           if (isRestaurantCloseByAndOpen(restaurantEntity, currentTime, latitude, longitude, servingRadiusInKms)) {
//             restaurants.add(modelMapper.map(restaurantEntity, Restaurant.class));
//           }
//         }
//         return restaurants;
//   }

//   /**
//    * Utility method to check if a restaurant is within the serving radius at a given time.
//    * @return boolean True if restaurant falls within serving radius and is open, false otherwise
//    */
//   private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
//       LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {

//     if (isOpenNow(currentTime, restaurantEntity)) {      
//       return GeoUtils.findDistanceInKm(latitude, longitude,
//           restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
//           < servingRadiusInKms;
//     }

//     return false;
//   }



// }

package com.crio.qeats.repositoryservices;

import ch.hsr.geohash.GeoHash;
import redis.clients.jedis.Jedis;
import com.crio.qeats.configs.RedisConfiguration;
import com.crio.qeats.dto.Restaurant;
import com.crio.qeats.globals.GlobalConstants;
import com.crio.qeats.models.RestaurantEntity;
import com.crio.qeats.repositories.RestaurantRepository;
import com.crio.qeats.utils.GeoLocation;
import com.crio.qeats.utils.GeoUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.inject.Provider;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.stereotype.Service;


@Service
public class RestaurantRepositoryServiceImpl implements RestaurantRepositoryService {


  @Autowired
  private RestaurantRepository restaurantRepository;

  @Autowired
  private RedisConfiguration redisConfiguration;

  @Autowired
  private MongoTemplate mongoTemplate;

  @Autowired
  private Provider<ModelMapper> modelMapperProvider;

  private boolean isOpenNow(LocalTime time, RestaurantEntity res) {
    LocalTime openingTime = LocalTime.parse(res.getOpensAt());
    LocalTime closingTime = LocalTime.parse(res.getClosesAt());

    return time.isAfter(openingTime) && time.isBefore(closingTime);
  }

  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objectives:
  // 1. Implement findAllRestaurantsCloseby.
  // 2. Remember to keep the precision of GeoHash in mind while using it as a key.
  // Check RestaurantRepositoryService.java file for the interface contract.
  
  public List<Restaurant> findAllRestaurantsMongo(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

        ModelMapper modelMapper = modelMapperProvider.get();
        List<RestaurantEntity> restaurantEntityList = restaurantRepository.findAll();
    
        List<Restaurant> restaurantList = new ArrayList<>();
        for (RestaurantEntity restaurantEntity : restaurantEntityList) {
    
          if (isOpenNow(currentTime, restaurantEntity)) {
            if (GeoUtils.findDistanceInKm(latitude, longitude,
                    restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
                    < servingRadiusInKms) {
              restaurantList.add(modelMapper.map(restaurantEntity, Restaurant.class));
            }
          }
        }
    
        return restaurantList;
    

  }

  
  private List<Restaurant> findAllRestaurantsCache(Double latitude, Double longitude,
      LocalTime currentTime, Double servingRadiusInKms) {
   
        List<Restaurant> restaurantList = new ArrayList<>();

        GeoLocation geoLocation = new GeoLocation(latitude, longitude);
        GeoHash geoHash = GeoHash.withCharacterPrecision(geoLocation.getLatitude(),
                geoLocation.getLongitude(), 7);
    
  


        Jedis jedis = null;
        try {
          jedis = redisConfiguration.getJedisPool().getResource();
          String jsonStringFromCache = jedis.get(geoHash.toBase32());
    
          if (jsonStringFromCache == null) {
            // Cache needs to be updated.
            String createdJsonString = "";
            try {
              restaurantList = findAllRestaurantsMongo(geoLocation.getLatitude(),
                  geoLocation.getLongitude(), currentTime, servingRadiusInKms);
              createdJsonString = new ObjectMapper().writeValueAsString(restaurantList);
            } catch (JsonProcessingException e) {
              e.printStackTrace();
            }
    
            // Do operations with jedis resource
            jedis.setex(geoHash.toBase32(), GlobalConstants.REDIS_ENTRY_EXPIRY_IN_SECONDS,
                    createdJsonString);
          } else {
            try {
              restaurantList = new ObjectMapper().readValue(jsonStringFromCache,
                      new TypeReference<List<Restaurant>>(){});
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        } finally {
          if (jedis != null) {
            jedis.close();
          }
        }
    
        return restaurantList;    
  }
  
  
  public List<Restaurant> findAllRestaurantsCloseBy(Double latitude,
      Double longitude, LocalTime currentTime, Double servingRadiusInKms) {

        if (redisConfiguration.isCacheAvailable()) {
          return findAllRestaurantsCache(latitude, longitude, currentTime, servingRadiusInKms);
        } else { 
          return findAllRestaurantsMongo(latitude, longitude, currentTime, servingRadiusInKms);
        }
    
    }


      //CHECKSTYLE:OFF
      //CHECKSTYLE:ON


    
  








  // TODO: CRIO_TASK_MODULE_NOSQL
  // Objective:
  // 1. Check if a restaurant is nearby and open. If so, it is a candidate to be returned.
  // NOTE: How far exactly is "nearby"?

  /**
   * Utility method to check if a restaurant is within the serving radius at a given time.
   * @return boolean True if restaurant falls within serving radius and is open, false otherwise
   */
  private boolean isRestaurantCloseByAndOpen(RestaurantEntity restaurantEntity,
      LocalTime currentTime, Double latitude, Double longitude, Double servingRadiusInKms) {
    if (isOpenNow(currentTime, restaurantEntity)) {
      return GeoUtils.findDistanceInKm(latitude, longitude,
          restaurantEntity.getLatitude(), restaurantEntity.getLongitude())
          < servingRadiusInKms;
    }

    return false;
  }



}



