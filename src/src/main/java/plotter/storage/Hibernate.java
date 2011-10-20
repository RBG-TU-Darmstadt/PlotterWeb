package plotter.storage;

import java.util.List;

import org.hibernate.Session;

import plotter.entities.Document;
import plotter.entities.User;

public class Hibernate {

	/** Default Class logger */

	public static void saveObject(Object o) {
		try {
			Session session = HibernateUtil.getSessionFactory()
					.getCurrentSession();
			session.beginTransaction();
			session.save(o);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void saveOrUpdateObject(Object o) {
		try {
			Session session = HibernateUtil.getSessionFactory()
					.getCurrentSession();
			session.beginTransaction();
			session.merge(o);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void deleteObject(Object o) {
		try {
			Session session = HibernateUtil.getSessionFactory()
					.getCurrentSession();
			session.beginTransaction();
			session.delete(o);
			session.getTransaction().commit();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unchecked")
	public static List<Document> getDocuments() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<Document> docs = session.createQuery("from Document").list();
		return docs;
	}

	@SuppressWarnings("unchecked")
	public static List<User> getUsers() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<User> users = session.createQuery("from User").list();
		return users;
	}

	@SuppressWarnings("unchecked")
	public static List<Document> getDocuments(String checkSum) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		List<Document> docs = (List<Document>) session
				.createQuery("from Document as d where d.hash = ?")
				.setString(0, checkSum).list();
		return docs;
	}
}
