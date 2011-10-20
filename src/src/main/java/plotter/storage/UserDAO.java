package plotter.storage;

import org.hibernate.Session;

import plotter.entities.User;

public class UserDAO {

	public static boolean exists(String tuid) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Long occ = (Long) session
				.createQuery("select count(*) from User as u where u.tuid = ?")
				.setString(0, tuid).uniqueResult();

		if (occ > 0) {
			return true;
		} else {
			return false;
		}
	}

	public static void create(User u) {
		Hibernate.saveObject(u);
	}

	public static void update(User u) {
		Hibernate.saveOrUpdateObject(u);
	}

	public static User get(String tuid) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		User u = (User) session.createQuery("from User where tuid=?")
				.setString(0, tuid).uniqueResult();
		return u;
	}

}
