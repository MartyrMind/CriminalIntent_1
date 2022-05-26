package com.hfad.criminalintent

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*
/*
UUID - вспомогательный класс Java, который предоставляет простой способ генерирования универсально-
уникальных идентификаторов
 */

/*
Room оздает структуру таблицы БД для приложения, основываясь на определенных сущностях
Сущности - классы моделей, аннотированные аннотацией @Entity. Room создаст таблицу базы данных для
любого класса с такой аннотацией
 */
@Entity
data class Crime(@PrimaryKey val id: UUID = UUID.randomUUID(),
                 var title: String = "",
                 var date: Date = Date(),
                 var isSolved: Boolean = false,
                 var suspect: String = "") {
                    //добавим вычисляемое свойство для получения имени файла
                    val photoFileName
                    get() = "IMG_$id.jpg"
                }
/*
    Каждая строка в таблице будет представлять собой отдельные преступления, а каждое свойство
    превратится в столбец

    @PrimaryKey - аннотация, указывающая, какой столбец в БД является первичным ключом, то есть
    таким столбцом, содержащим уникальные данные для каждой записи.
data class Crime(val id: UUID = UUID.randomUUID(), var title: String = "",
        var date : Date = Date(), var isSolved: Boolean = false,
             var requiresPolice: Boolean = false)
*/

