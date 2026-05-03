package com.iptv.tv.data.local.dao;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomDatabaseKt;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import com.iptv.tv.data.local.ContentTypeConverters;
import com.iptv.tv.data.local.entity.StreamEntity;
import com.iptv.tv.domain.model.ContentType;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class StreamDao_Impl implements StreamDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<StreamEntity> __insertionAdapterOfStreamEntity;

  private final ContentTypeConverters __contentTypeConverters = new ContentTypeConverters();

  private final SharedSQLiteStatement __preparedStmtOfDeleteByCategory;

  public StreamDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfStreamEntity = new EntityInsertionAdapter<StreamEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `streams` (`id`,`name`,`categoryId`,`type`,`streamUrl`,`posterUrl`,`epgChannelId`,`containerExtension`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final StreamEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindString(3, entity.getCategoryId());
        final String _tmp = __contentTypeConverters.fromContentType(entity.getType());
        statement.bindString(4, _tmp);
        statement.bindString(5, entity.getStreamUrl());
        if (entity.getPosterUrl() == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, entity.getPosterUrl());
        }
        if (entity.getEpgChannelId() == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, entity.getEpgChannelId());
        }
        statement.bindString(8, entity.getContainerExtension());
        statement.bindLong(9, entity.getUpdatedAt());
      }
    };
    this.__preparedStmtOfDeleteByCategory = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM streams WHERE categoryId = ? AND type = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertAll(final List<StreamEntity> streams,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfStreamEntity.insert(streams);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object replaceByCategory(final String categoryId, final ContentType type,
      final List<StreamEntity> streams, final Continuation<? super Unit> $completion) {
    return RoomDatabaseKt.withTransaction(__db, (__cont) -> StreamDao.DefaultImpls.replaceByCategory(StreamDao_Impl.this, categoryId, type, streams, __cont), $completion);
  }

  @Override
  public Object deleteByCategory(final String categoryId, final ContentType type,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteByCategory.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, categoryId);
        _argIndex = 2;
        final String _tmp = __contentTypeConverters.fromContentType(type);
        _stmt.bindString(_argIndex, _tmp);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteByCategory.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<StreamEntity>> getStreamsByCategory(final String categoryId,
      final ContentType type) {
    final String _sql = "SELECT * FROM streams WHERE categoryId = ? AND type = ? ORDER BY name ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    _statement.bindString(_argIndex, categoryId);
    _argIndex = 2;
    final String _tmp = __contentTypeConverters.fromContentType(type);
    _statement.bindString(_argIndex, _tmp);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"streams"}, new Callable<List<StreamEntity>>() {
      @Override
      @NonNull
      public List<StreamEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfCategoryId = CursorUtil.getColumnIndexOrThrow(_cursor, "categoryId");
          final int _cursorIndexOfType = CursorUtil.getColumnIndexOrThrow(_cursor, "type");
          final int _cursorIndexOfStreamUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "streamUrl");
          final int _cursorIndexOfPosterUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "posterUrl");
          final int _cursorIndexOfEpgChannelId = CursorUtil.getColumnIndexOrThrow(_cursor, "epgChannelId");
          final int _cursorIndexOfContainerExtension = CursorUtil.getColumnIndexOrThrow(_cursor, "containerExtension");
          final int _cursorIndexOfUpdatedAt = CursorUtil.getColumnIndexOrThrow(_cursor, "updatedAt");
          final List<StreamEntity> _result = new ArrayList<StreamEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final StreamEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final String _tmpCategoryId;
            _tmpCategoryId = _cursor.getString(_cursorIndexOfCategoryId);
            final ContentType _tmpType;
            final String _tmp_1;
            _tmp_1 = _cursor.getString(_cursorIndexOfType);
            _tmpType = __contentTypeConverters.toContentType(_tmp_1);
            final String _tmpStreamUrl;
            _tmpStreamUrl = _cursor.getString(_cursorIndexOfStreamUrl);
            final String _tmpPosterUrl;
            if (_cursor.isNull(_cursorIndexOfPosterUrl)) {
              _tmpPosterUrl = null;
            } else {
              _tmpPosterUrl = _cursor.getString(_cursorIndexOfPosterUrl);
            }
            final String _tmpEpgChannelId;
            if (_cursor.isNull(_cursorIndexOfEpgChannelId)) {
              _tmpEpgChannelId = null;
            } else {
              _tmpEpgChannelId = _cursor.getString(_cursorIndexOfEpgChannelId);
            }
            final String _tmpContainerExtension;
            _tmpContainerExtension = _cursor.getString(_cursorIndexOfContainerExtension);
            final long _tmpUpdatedAt;
            _tmpUpdatedAt = _cursor.getLong(_cursorIndexOfUpdatedAt);
            _item = new StreamEntity(_tmpId,_tmpName,_tmpCategoryId,_tmpType,_tmpStreamUrl,_tmpPosterUrl,_tmpEpgChannelId,_tmpContainerExtension,_tmpUpdatedAt);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
