package Dao;

import Pojo.Rooms;
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

public class RoomsDao implements Dao<Rooms, Long> {
    private static final Logger logger = LoggerFactory.getLogger(RoomsDao.class);

    private final static String FIND_ALL = "SELECT * FROM rooms";
    private final static String FIND_BY_ID = "SELECT * FROM rooms WHERE room_id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM rooms WHERE room_id=?";
    private final static String SAVE = "INSERT INTO rooms(type_id,number) " +
            "VALUES (?, ?)";

    @Override
    public Rooms findById(Long id) throws Exception {
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_BY_ID);

        statement.setLong(1, id);

        try (ResultSet resultSet = statement.executeQuery()){
            if (resultSet.next()) {
                Rooms room = new Rooms();
                room.setRoomId(resultSet.getLong("room_id"));
                room.setNumber(resultSet.getLong("number"));
                room.setTypeId(resultSet.getLong("type_id"));
                return room;
            }
        }
        return null;
    }

    @Override
    public Collection<Rooms> findByRoomId(Long aLong) throws Exception {
        return List.of();
    }

    @Override
    public Collection<Rooms> findAll() throws Exception {
        List<Rooms> list;
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_ALL);
        ResultSet rs = statement.executeQuery();

        list = mapper(rs);

        return list;
    }

    private List<Rooms> mapper(ResultSet rs) {
        List<Rooms> list = new ArrayList<>();
        try {
            while (rs.next()) {
                list.add(new Rooms(rs.getLong("room_id"),
                        rs.getLong("number"),
                        rs.getLong("type_id")
                ));
            }
        } catch (SQLException e) {
            logger.error("Ошибка SQL", e);
        }
        return list;
    }

    @Override
    public Rooms save(Rooms room) throws Exception {
        logger.debug("SQL save: {}", SAVE);
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(SAVE, new String[]{"room_id"});
        statement.setLong(1, room.getTypeId());
        statement.setLong(2, room.getNumber() );

        statement.executeUpdate();

        try (ResultSet rs = statement.getGeneratedKeys()) {
            if (rs.next()) {
                room.setRoomId(rs.getLong("room_id"));
            }
        }

        return room;
    }

    @Override
    public Rooms update(Rooms entity) {
        logger.debug("SQL update");
        return null;
    }

    @Override
    public void deleteById(long id) throws Exception {
        logger.debug("SQL delete id={}", id);
        Connection connection = DBHelper.getConnection();
        try (PreparedStatement orders = connection.prepareStatement(
                "DELETE FROM orders WHERE pc_id IN (SELECT pc_id FROM pcs WHERE room_id = ?)")) {
            orders.setLong(1, id);
            orders.executeUpdate();
        }
        try (PreparedStatement pcs = connection.prepareStatement("DELETE FROM pcs WHERE room_id = ?")) {
            pcs.setLong(1, id);
            pcs.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public List<Rooms> search(String query) {
        return List.of();
    }
}
