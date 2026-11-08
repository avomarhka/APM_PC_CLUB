package Dao;

import Models.RoomTypes;
import javafx.beans.property.LongProperty;
import utils.DBHelper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class RoomTypesDao implements Dao<RoomTypes, Long>{
    private final static String FIND_ALL = "SELECT * FROM roomTypes";
    private final static String FIND_BY_ID = "SELECT * FROM roomTypes WHERE type_id = ?";

    private static final String DELETE_BY_ID = "DELETE FROM roomTypes WHERE type_id=?";
    private final static String SAVE = "INSERT INTO roomTypes(name) " +
            "VALUES (?)";



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
                list.add(new RoomTypes(rs.getInt("type_id"),
                        rs.getString("name")));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return list;
    }

    @Override
    public RoomTypes save(RoomTypes roomTypes)  throws Exception{
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(SAVE, new String[]{"type_id"});
            statement.setString(1, roomTypes.getName());

            statement.executeUpdate();

            try (ResultSet rs = statement.getGeneratedKeys()) {
                if (rs.next()) {
                    roomTypes.setId(rs.getLong("type_id"));
                }
            }

        return roomTypes;
    }

    @Override
    public RoomTypes update(RoomTypes entity) {
        return null;
    }


    @Override
    public void deleteById(long id) throws Exception {
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID);
        statement.setLong(1, id);
        statement.executeUpdate();

    }

    @Override
    public List<RoomTypes> search(String query) {
        return List.of();
    }
}
