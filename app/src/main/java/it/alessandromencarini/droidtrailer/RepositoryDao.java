package it.alessandromencarini.droidtrailer;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;

import de.greenrobot.dao.AbstractDao;
import de.greenrobot.dao.Property;
import de.greenrobot.dao.internal.DaoConfig;

import it.alessandromencarini.droidtrailer.Repository;

// THIS CODE IS GENERATED BY greenDAO, DO NOT EDIT.
/** 
 * DAO for table REPOSITORY.
*/
public class RepositoryDao extends AbstractDao<Repository, Long> {

    public static final String TABLENAME = "REPOSITORY";

    /**
     * Properties of entity Repository.<br/>
     * Can be used for QueryBuilder and for referencing column names.
    */
    public static class Properties {
        public final static Property Id = new Property(0, Long.class, "id", true, "_id");
        public final static Property FullName = new Property(1, String.class, "fullName", false, "FULL_NAME");
        public final static Property RemoteId = new Property(2, Long.class, "remoteId", false, "REMOTE_ID");
        public final static Property Selected = new Property(3, Boolean.class, "selected", false, "SELECTED");
    };

    private DaoSession daoSession;


    public RepositoryDao(DaoConfig config) {
        super(config);
    }
    
    public RepositoryDao(DaoConfig config, DaoSession daoSession) {
        super(config, daoSession);
        this.daoSession = daoSession;
    }

    /** Creates the underlying database table. */
    public static void createTable(SQLiteDatabase db, boolean ifNotExists) {
        String constraint = ifNotExists? "IF NOT EXISTS ": "";
        db.execSQL("CREATE TABLE " + constraint + "'REPOSITORY' (" + //
                "'_id' INTEGER PRIMARY KEY ," + // 0: id
                "'FULL_NAME' TEXT," + // 1: fullName
                "'REMOTE_ID' INTEGER," + // 2: remoteId
                "'SELECTED' INTEGER);"); // 3: selected
    }

    /** Drops the underlying database table. */
    public static void dropTable(SQLiteDatabase db, boolean ifExists) {
        String sql = "DROP TABLE " + (ifExists ? "IF EXISTS " : "") + "'REPOSITORY'";
        db.execSQL(sql);
    }

    /** @inheritdoc */
    @Override
    protected void bindValues(SQLiteStatement stmt, Repository entity) {
        stmt.clearBindings();
 
        Long id = entity.getId();
        if (id != null) {
            stmt.bindLong(1, id);
        }
 
        String fullName = entity.getFullName();
        if (fullName != null) {
            stmt.bindString(2, fullName);
        }
 
        Long remoteId = entity.getRemoteId();
        if (remoteId != null) {
            stmt.bindLong(3, remoteId);
        }
 
        Boolean selected = entity.getSelected();
        if (selected != null) {
            stmt.bindLong(4, selected ? 1l: 0l);
        }
    }

    @Override
    protected void attachEntity(Repository entity) {
        super.attachEntity(entity);
        entity.__setDaoSession(daoSession);
    }

    /** @inheritdoc */
    @Override
    public Long readKey(Cursor cursor, int offset) {
        return cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0);
    }    

    /** @inheritdoc */
    @Override
    public Repository readEntity(Cursor cursor, int offset) {
        Repository entity = new Repository( //
            cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0), // id
            cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1), // fullName
            cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2), // remoteId
            cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0 // selected
        );
        return entity;
    }
     
    /** @inheritdoc */
    @Override
    public void readEntity(Cursor cursor, Repository entity, int offset) {
        entity.setId(cursor.isNull(offset + 0) ? null : cursor.getLong(offset + 0));
        entity.setFullName(cursor.isNull(offset + 1) ? null : cursor.getString(offset + 1));
        entity.setRemoteId(cursor.isNull(offset + 2) ? null : cursor.getLong(offset + 2));
        entity.setSelected(cursor.isNull(offset + 3) ? null : cursor.getShort(offset + 3) != 0);
     }
    
    /** @inheritdoc */
    @Override
    protected Long updateKeyAfterInsert(Repository entity, long rowId) {
        entity.setId(rowId);
        return rowId;
    }
    
    /** @inheritdoc */
    @Override
    public Long getKey(Repository entity) {
        if(entity != null) {
            return entity.getId();
        } else {
            return null;
        }
    }

    /** @inheritdoc */
    @Override    
    protected boolean isEntityUpdateable() {
        return true;
    }
    
}