package Dao;

import Models.Clients;
import Models.Orders;
import Models.Rooms;
import utils.DBHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class OrdersDao implements Dao<Orders, Long> {
    private static final Logger logger = LoggerFactory.getLogger(OrdersDao.class);

    private final static String FIND_ALL = "SELECT * FROM orders ";
    private final static String SAVE = "INSERT INTO orders(order_number, client_id, pc_id, date_time_start, date_time_end, cost) " +
            "VALUES (?, ?, ?, ?, ?, ?)";
    private final static String SEARCH = "SELECT * FROM orders o JOIN clients c ON o.client_id = c.clients_id WHERE o.order_number ILIKE ?   OR c.surname ILIKE  ? OR c.name ILIKE ? OR c.patronymic ILIKE ? OR c.number_phone ILIKE ?";

    private static final String DELETE_BY_ID = "DELETE FROM orders WHERE order_id = ?";
    private static final String UPDATE = "UPDATE orders SET order_number = ?, client_id = ?, pc_id = ?, date_time_start = ?, date_time_end = ?, cost = ? WHERE order_id = ?";

    @Override
    public Orders findById(Long aLong) throws Exception {
        return null;
    }

    @Override
    public Collection<Orders> findByRoomId(Long aLong) throws Exception {
        return List.of();
    }

    @Override
    public Collection<Orders> findAll() throws Exception {
        List<Orders> list;
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_ALL);
        ResultSet rs = statement.executeQuery();

        list = mapper(rs);

        return list;
    }

    private List<Orders> mapper(ResultSet rs) {
        List<Orders> list = new ArrayList<>();
        try {
            while (rs.next()) {
                list.add(new Orders(rs.getLong("order_id"),
                        rs.getTimestamp("date_time_start").toLocalDateTime(),
                        rs.getTimestamp("date_time_end").toLocalDateTime(),
                        rs.getLong("pc_id"),
                        rs.getLong("client_id"),
                        rs.getDouble("cost"),
                        rs.getString("order_number")
                ));
            }
        } catch (SQLException e) {
            logger.error("Ошибка SQL", e);
        }
        return list;
    }
    @Override
    public Orders save(Orders order) throws Exception {
        logger.debug("SQL save: {}", SAVE);
        Connection connection = DBHelper.getConnection();
        PreparedStatement ps = connection.prepareStatement(SAVE, new String[]{"order_id"});

        ps.setString(1, order.getOrderNumber());
        ps.setLong(2, order.getClientId());
        ps.setLong(3, order.getPcId());
        ps.setTimestamp(4, java.sql.Timestamp.valueOf(order.getDateTimeStart()));
        ps.setTimestamp(5, java.sql.Timestamp.valueOf(order.getDateTimeEnd()));
        ps.setDouble(6, order.getCost());

        ps.executeUpdate();

        try (ResultSet rs = ps.getGeneratedKeys()) {
            if (rs.next()) {
                order.setOrderId(rs.getLong("order_id"));
            }
        }
        return order;
    }

    @Override
    public Orders update(Orders order) throws Exception {
        logger.debug("SQL update");
        Connection connection = DBHelper.getConnection();
        PreparedStatement ps = connection.prepareStatement(UPDATE);
        ps.setString(1, order.getOrderNumber());
        ps.setLong(2, order.getClientId());
        ps.setLong(3, order.getPcId());
        ps.setTimestamp(4, java.sql.Timestamp.valueOf(order.getDateTimeStart()));
        ps.setTimestamp(5, java.sql.Timestamp.valueOf(order.getDateTimeEnd()));
        ps.setDouble(6, order.getCost());
        ps.setLong(7, order.getOrderId());
        ps.executeUpdate();
        return order;
    }

    @Override
    public void deleteById(long id) throws Exception {
        logger.debug("SQL delete id={}", id);
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID);
        statement.setLong(1, id);
        statement.executeUpdate();


    }

    @Override
    public List<Orders> search(String value) throws Exception {
        List<Orders> result = new ArrayList<>();
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(SEARCH);

        String search = "%" + value + "%";
        statement.setString(1,search);
        statement.setString(2, search);
        statement.setString(3, search);
        statement.setString(4, search);
        statement.setString(5, search);


        try (ResultSet rs = statement.executeQuery()) {
            result = mapper(rs);
        }
        return result;


    }

    public int getTodayOrderCount() throws Exception {

        String sql = "SELECT COUNT(*) FROM orders WHERE DATE(date_time_start) = CURRENT_DATE";
        Connection connection = DBHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt(1);
            }

        return 0;
    }

    public boolean isPcBusy(long pcId, LocalDateTime start, LocalDateTime end) throws Exception {
        String sql = "SELECT COUNT(*) FROM orders WHERE pc_id = ? " +
                "AND date_time_start < ? AND date_time_end > ?";
        Connection connection = DBHelper.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
            ps.setLong(1, pcId);
            ps.setTimestamp(2, java.sql.Timestamp.valueOf(end));
            ps.setTimestamp(3, java.sql.Timestamp.valueOf(start));
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }

        return false;
    }
}
