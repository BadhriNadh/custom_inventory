package com.custom.inventory.repository;

import com.custom.inventory.model.Item;
import com.custom.inventory.model.Store;
import com.custom.inventory.model.Zone;
import com.custom.inventory.protocol.RequestItem;
import com.custom.inventory.protocol.RequestStore;
import com.custom.inventory.protocol.RequestZone;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;

@Repository
public class StoreRepository {
    @Autowired
    MongoTemplate mongoTemplate;

    public Store findStore(String name, String email){
        Pattern namePattern = Pattern.compile(name, Pattern.CASE_INSENSITIVE);
        Pattern emailPattern = Pattern.compile(email, Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(namePattern).and("email").regex(emailPattern));

        return mongoTemplate.findOne(query, Store.class);
    }

    public Store createStore(RequestStore requestStore){
        Store newStore = new Store(requestStore.getName(), requestStore.getEmail(), List.of());
        mongoTemplate.insert(newStore);
        return newStore;
    }

    public void addZone(RequestZone requestZone){
        Pattern namePattern = Pattern.compile(requestZone.getName(), Pattern.CASE_INSENSITIVE);
        Pattern emailPattern = Pattern.compile(requestZone.getEmail(), Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(namePattern).and("email").regex(emailPattern));

        Zone newZone = new Zone(requestZone.getZoneName(), List.of());

        Update update = new Update().push("zones", newZone);

        mongoTemplate.updateFirst(query, update, Store.class);
    }

    public void addItemToZone(RequestItem requestItem) {
        Pattern namePattern = Pattern.compile(requestItem.getName(), Pattern.CASE_INSENSITIVE);
        Pattern emailPattern = Pattern.compile(requestItem.getEmail(), Pattern.CASE_INSENSITIVE);
        Pattern zoneNamePattern = Pattern.compile(requestItem.getZoneName(), Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(namePattern)
                .and("email").regex(emailPattern)
                .and("zones.zoneName").regex(zoneNamePattern));

        Item newItem = new Item(requestItem.getItemName(), requestItem.getCount());

        Update update = new Update().push("zones.$.items", newItem);

        mongoTemplate.updateFirst(query, update, Store.class);
    }

    public void updateItemCountInZone(RequestItem requestItem) {
        Pattern namePattern = Pattern.compile(requestItem.getName(), Pattern.CASE_INSENSITIVE);
        Pattern emailPattern = Pattern.compile(requestItem.getEmail(), Pattern.CASE_INSENSITIVE);
        Pattern zoneNamePattern = Pattern.compile(requestItem.getZoneName(), Pattern.CASE_INSENSITIVE);
        Pattern itemNamePattern = Pattern.compile(requestItem.getItemName(), Pattern.CASE_INSENSITIVE);
        Query query = new Query(Criteria.where("name").regex(namePattern)
                .and("email").regex(emailPattern)
                .and("zones.zoneName").regex(zoneNamePattern)
                .and("zones.items.itemName").regex(itemNamePattern));

        Update update = new Update().set("zones.$.items.$[item].count", requestItem.getCount());
        update.filterArray(Criteria.where("item.itemName").is(itemNamePattern));

        mongoTemplate.updateFirst(query, update, Store.class);
    }
}