package Dao;

import Models.RoomTypes;
import javafx.beans.property.LongProperty;
import utils.DBHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoomTypesDao implements Dao<RoomTypes, Long>{
    private static final Logger logger = LoggerFactory.getLogger(RoomTypesDao.class);

    private final static String FIND_ALL = "SELECT * FROM roomTypes";
    private final static String FIND_BY_ID = "SELECT * FROM roomTypes WHERE type_id = ?";

    private static final String DELETE_BY_ID = "DELETE FROM roomTypes WHERE type_id=?";
    private final static String SAVE = "INSERT INTO roomTypes(name, pc_count) VALUES (?, ?)";
    private final static String UPDATE = "UPDATE roomTypes SET name = ?, pc_count = ? WHERE type_id = ?";



    @Override
    public RoomTypes findById(Long id) throws Exception {
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_BY_ID);

        statement.setLong(1, id);

        try (ResultSet resultSet = statement.executeQuery()){
                if (resultSet.next()) {
                    RoomTypes roomTypes = new RoomTypes();
                    roomTypes.setId(resultSet.getLong("type_id"));
                    roomTypes.setName(resultSet.getString("name"));
                    roomTypes.setPcCount(resultSet.getInt("pc_count"));
                    return roomTypes;
                }
        }

        return null;
    }

    @Override
    public Collection<RoomTypes> findByRoomId(Long roomId) throws Exception {
        return List.of();
    }



    @Override
    public Collection<RoomTypes> findAll() throws Exception{
        List<RoomTypes> list;
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_ALL);
        ResultSet rs = statement.executeQuery();

        list = mapper(rs);

        return list;
    }

    private List<RoomTypes> mapper(ResultSet rs) {
        List<RoomTypes> list = new ArrayList<>();
        try {
            while (rs.next()) {
                RoomTypes type = new RoomTypes(rs.getInt("type_id"), rs.getString("name"));
                type.setPcCount(rs.getInt("pc_count"));
                list.add(type);
            }
        } catch (SQLException e) {
            logger.error("Ошибка SQL", e);
        }
        return list;
    }

    @Override
    public RoomTypes save(RoomTypes roomTypes)  throws Exception{
        logger.debug("SQL save: {}", SAVE);
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(SAVE, new String[]{"type_id"});
            statement.setString(1, roomTypes.getName());
            statement.setInt(2, roomTypes.getPcCount());

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    roomTypes.setId(rs.getLong("type_id"));
                }
            }

        return roomTypes;
    }

    @Override
    public RoomTypes update(RoomTypes roomTypes) throws Exception {
        logger.debug("SQL update");
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(UPDATE);
        statement.setString(1, roomTypes.getName());
        statement.setInt(2, roomTypes.getPcCount());
        statement.setLong(3, roomTypes.getId());
        statement.executeUpdate();
        return roomTypes;
    }


    @Override
    public void deleteById(long id) throws Exception {
        logger.debug("SQL delete id={}", id);
        Connection connection = DBHelper.getConnection();
        try (PreparedStatement orders = connection.prepareStatement(
                "DELETE FROM orders WHERE pc_id IN (SELECT pc_id FROM pcs WHERE room_id IN (SELECT room_id FROM rooms WHERE type_id = ?))")) {
            orders.setLong(1, id);
            orders.executeUpdate();
        }
        try (PreparedStatement pcs = connection.prepareStatement(
                "DELETE FROM pcs WHERE room_id IN (SELECT room_id FROM rooms WHERE type_id = ?)")) {
            pcs.setLong(1, id);
            pcs.executeUpdate();
        }
        try (PreparedStatement prices = connection.prepareStatement("DELETE FROM prices WHERE type_id = ?")) {
            prices.setLong(1, id);
            prices.executeUpdate();
        }
        try (PreparedStatement rooms = connection.prepareStatement("DELETE FROM rooms WHERE type_id = ?")) {
            rooms.setLong(1, id);
            rooms.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public List<RoomTypes> search(String query) {
        return List.of();
    }
}
