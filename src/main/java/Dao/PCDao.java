package Dao;

import Pojo.PC;
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

public class PCDao implements Dao<PC, Long> {
    private static final Logger logger = LoggerFactory.getLogger(PCDao.class);

    private final static String FIND_ALL = "SELECT * FROM pcs";
    private final static String FIND_BY_ID = "SELECT * FROM pcs WHERE pc_id = ?";
    private final static String FIND_BY_ROOM_ID = "SELECT * FROM pcs WHERE room_id = ?";
    private final static String SAVE = "INSERT INTO pcs(room_id, number,available) " +
            "VALUES (?,?, ?)";
    private static final String DELETE_BY_ID = "DELETE FROM pcs WHERE pc_id=?";
    private static final String UPDATE = "UPDATE pcs SET room_id = ?, number = ?, available = ? WHERE pc_id = ?";

    @Override
    public PC findById(Long id)throws Exception {
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_BY_ID);

        statement.setLong(1, id);

        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                PC pc = new PC();
                pc.setPcId(resultSet.getLong("pc_id"));
                pc.setNumberPC(resultSet.getLong("number"));
                pc.setAvailable(resultSet.getBoolean("available"));
                pc.setRoomId(resultSet.getLong("room_id"));
                return pc;
            }
        }

        return null;
    }

    @Override
    public Collection<PC>  findByRoomId(Long roomId) throws Exception {
        List<PC> list = new ArrayList<>();
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_BY_ROOM_ID);
            statement.setLong(1, roomId);
            try (ResultSet rs = statement.executeQuery()) {
                list = mapper(rs);
            }

        return  list;
    }




    @Override
    public Collection<PC> findAll() throws SQLException {
        List<PC> list;
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_ALL);
        ResultSet rs = statement.executeQuery();

        list = mapper(rs);

        return list;
    }

    private List<PC> mapper(ResultSet rs) {
        List<PC> list = new ArrayList<>();
        try {
            while (rs.next()) {
                list.add(new PC(rs.getLong("pc_id"),
                        rs.getLong("number"),
                        rs.getBoolean("available"),
                        rs.getLong("room_id")
                ));
            }
        } catch (SQLException e) {
            logger.error("Ошибка SQL", e);
        }
        return list;
    }

    @Override
    public PC save(PC pc) throws Exception {
        logger.debug("SQL save: {}", SAVE);
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(SAVE, new String[]{"pc_id"});
        statement.setLong(1, pc.getRoomId());
        statement.setLong(2, pc.getNumberPC() );
        statement.setBoolean(3, pc.getAvailable());

        statement.executeUpdate();

        try (ResultSet rs = statement.getGeneratedKeys()) {
            if (rs.next()) {
                pc.setPcId(rs.getLong("pc_id"));
            }
        }

        return pc;
    }

    @Override
    public PC update(PC pc) throws Exception {
        logger.debug("SQL update");
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(UPDATE);
        statement.setLong(1, pc.getRoomId());
        statement.setLong(2, pc.getNumberPC());
        statement.setBoolean(3, pc.getAvailable());
        statement.setLong(4, pc.getPcId());
        statement.executeUpdate();
        return pc;
    }



    @Override
    public void deleteById(long  id) throws Exception {
        logger.debug("SQL delete id={}", id);
        Connection connection = DBHelper.getConnection();
        try (PreparedStatement orders = connection.prepareStatement("DELETE FROM orders WHERE pc_id = ?")) {
            orders.setLong(1, id);
            orders.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public List<PC> search(String query) {
        return List.of();
    }
}
