package Dao;

import Pojo.Clients;
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

public class ClientsDao implements Dao<Clients, Long>{
    private static final Logger logger = LoggerFactory.getLogger(ClientsDao.class);

    private final static String FIND_ALL = "SELECT * FROM clients";
    private final static String FIND_BY_ID = "SELECT * FROM clients WHERE clients_id = ?";
    private static final String DELETE_BY_ID = "DELETE FROM clients WHERE clients_id = ?";
    private final static String UPDATE = "UPDATE clients SET surname=?, name=?, patronymic=?, number_phone=?, birthday = ? WHERE clients_id=?"; //TODO age
    private final static String SAVE = "INSERT INTO clients(surname, name, patronymic, number_phone, birthday) " +
            "VALUES (?,?,?,?,?)";
    private final static String SEARCH = "SELECT * FROM clients WHERE surname ILIKE ? OR name ILIKE ? OR patronymic ILIKE ? or number_phone ILIKE ?";
    private final static String SEARCH_BY_PHONE = "SELECT * FROM clients WHERE number_phone LIKE ?";


    @Override
    public Clients findById(Long id) throws Exception {
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_BY_ID);
        statement.setLong(1, id);
        try (ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                Clients clients = new Clients();
                clients.setClientId(resultSet.getLong("clients_id"));
                clients.setSurname(resultSet.getString("surname"));
                clients.setName(resultSet.getString("name"));
                clients.setPatronymic(resultSet.getString("patronymic"));
                clients.setNumberPhone(resultSet.getString("number_phone"));
                java.sql.Date sqlDate = resultSet.getDate("birthday");
                java.time.LocalDate birthday = (sqlDate != null) ? sqlDate.toLocalDate() : null;
                clients.setBirthday(birthday);
                return clients;

            }
        }
        return null;
    }

    @Override
    public Collection<Clients> findByRoomId(Long aLong) throws Exception {
        return List.of();
    }

    @Override
    public Collection<Clients> findAll() throws Exception {
        List<Clients> list;
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(FIND_ALL);
        ResultSet rs = statement.executeQuery();

        list = mapper(rs);

        return list;
    }

    private List<Clients> mapper(ResultSet rs) {
        List<Clients> list = new ArrayList<>();
        try {
            while (rs.next()) {
                java.sql.Date sqlDate = rs.getDate("birthday");
                java.time.LocalDate birthday = (sqlDate != null) ? sqlDate.toLocalDate() : null;

                list.add(new Clients(rs.getLong("clients_id"),
                        rs.getString("surname"),
                        rs.getString("name"),
                        rs.getString("patronymic"),
                        rs.getString("number_phone"),
                        birthday
                ));
            }
        } catch (SQLException e) {
            logger.error("Ошибка SQL", e);
        }
        return list;
    }

    @Override
    public Clients save(Clients clients) throws Exception {
        logger.debug("SQL save: {}", SAVE);
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(SAVE, new String[]{"clients_id"});
        statement.setString(1, clients.getSurname());
        statement.setString(2, clients.getName() );
        statement.setString(3, clients.getPatronymic());
        statement.setString(4, clients.getNumberPhone());
        if (clients.getBirthday() != null) {
            statement.setDate(5, java.sql.Date.valueOf(clients.getBirthday()));
        } else {
            statement.setNull(5, java.sql.Types.DATE);
        }

        statement.executeUpdate();

        try (ResultSet rs = statement.getGeneratedKeys()) {
            if (rs.next()) {
                clients.setClientId(rs.getLong("clients_id"));
            }
        }

        return clients;
    }

    @Override
    public Clients update(Clients clients)throws Exception{
        logger.debug("SQL update");
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(UPDATE);


        statement.setString(1, clients.getSurname());
        statement.setString(2, clients.getName());
        statement.setString(3, clients.getPatronymic());
        statement.setString(4, clients.getNumberPhone());
        if (clients.getBirthday() != null) {
            statement.setDate(5, java.sql.Date.valueOf(clients.getBirthday()));
        } else {
            statement.setNull(5, java.sql.Types.DATE);
        }
        statement.setLong(6, clients.getClientId());


        statement.executeUpdate();


        return clients;
    }

    @Override
    public void deleteById(long id) throws Exception {
        logger.debug("SQL delete id={}", id);
        Connection connection = DBHelper.getConnection();
        try (PreparedStatement orders = connection.prepareStatement("DELETE FROM orders WHERE client_id = ?")) {
            orders.setLong(1, id);
            orders.executeUpdate();
        }
        try (PreparedStatement statement = connection.prepareStatement(DELETE_BY_ID)) {
            statement.setLong(1, id);
            statement.executeUpdate();
        }
    }

    @Override
    public List<Clients> search(String value) throws Exception {
        List<Clients> result = new ArrayList<>();
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(SEARCH);

        String search = "%" + value + "%";
        statement.setString(1,search);
        statement.setString(2, search);
        statement.setString(3, search);
        statement.setString(4, search);


        try (ResultSet rs = statement.executeQuery()) {
            result = mapper(rs);
        }
        return result;
    }

    public Collection<Clients> searchByPhone(String numberPhone) throws Exception {
        Connection connection = DBHelper.getConnection();
        PreparedStatement statement = connection.prepareStatement(SEARCH_BY_PHONE);

        statement.setString(1, "%" + numberPhone + "%");
        try (ResultSet rs = statement.executeQuery()) {
            return mapper(rs); // Возвращаем список
        }

    }


}
