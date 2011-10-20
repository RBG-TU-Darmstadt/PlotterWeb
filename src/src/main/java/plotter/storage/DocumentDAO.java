package plotter.storage;

import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;

import plotter.entities.Document;

public class DocumentDAO {

	public static boolean hashExists(String checkSum) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Long occ = (Long) session
				.createQuery(
						"select count(*) from Document as m where m.hash = ?")
				.setString(0, checkSum).uniqueResult();

		if (occ > 0) {
			return true;
		} else {
			return false;
		}

	}

	public static Document getDocumentByHash(String hash) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Document resultSet = (Document) session
				.createQuery("from Document where hash=?").setString(0, hash)
				.uniqueResult();
		return resultSet;
	}

	public static Document getDocumentById(String id) {
		Long longId = Long.valueOf(id);
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		Document result = (Document) session
				.createQuery("from Document where doc_id=?").setLong(0, longId)
				.uniqueResult();
		return result;
	}

	@SuppressWarnings("unchecked")
	public static List<Document> getLastJobsFromUser(Long user_id) {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query query = session.createQuery(
				"from Document where user_id=? order by doc_printdate asc")
				.setParameter(0, user_id);
		List<Document> list = query.setFirstResult(query.list().size() - 5)
				.setMaxResults(5).list();

		return list;
	}

	public static void create(Document d) {
		Hibernate.saveObject(d);
	}

	@SuppressWarnings("unchecked")
	public static List<Document> getAllJobs() {
		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();

		Query query = session.createQuery("from Document");
		List<Document> list = query.list();

		return list;
	}

	public static void deleteJobs(List<Integer> documentIds) {

		Session session = HibernateUtil.getSessionFactory().getCurrentSession();
		session.beginTransaction();
		for (Integer id : documentIds) {
			// System.out.println("trying to delete: " + id);
			Query query = session.createQuery("from Document where doc_id =? ");
			query.setParameter(0, id);
			session.delete(query.list().get(0));
		}
		session.getTransaction().commit();
	}
}
