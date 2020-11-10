/*
* DatabaseWizard
* Native Excel Module Library
* 
* Powered by xlnt
* https://github.com/tfussell/xlnt
* MIT License
* 
* Created on 24/10/20
* (c) Mikhail K., 2020
*/

#include <iostream>
#include <string>
#include <map>
#include <vector>
#include <set>

#define XLNT_STATIC 1
#include <xlnt/xlnt.hpp>

#include "JNIExcel.h"

/* ------------------------- Java Utils -------------------------*/
#define jprint(x) std::cout << "[Excel Native] " + std::to_string(x) << std::endl;
bool exception_handling_enabled = true;
void jthrow_exception(JNIEnv* env, std::string message)
{
    jclass jexception_class = env->FindClass("ru/mkr/dbw/exceptions/NativeLibraryException");
    env->ThrowNew(jexception_class, "TODO: message");
    env->DeleteLocalRef(jexception_class);
}
/* --------------------------------------------------------------*/

std::map<jint, xlnt::workbook*> loaded_books;
void close_book(xlnt::workbook* book)
{
    book->clear();
    delete book;
}

JNIEXPORT jboolean JNICALL Java_ru_mkr_dbw_modules_NativeExcelModule__1setExceptionHandlingEnabled
(JNIEnv*, jobject, jboolean jb)
{
    exception_handling_enabled = jb;
    return exception_handling_enabled;
}

JNIEXPORT jboolean JNICALL Java_ru_mkr_dbw_modules_NativeExcelModule__1readExcelBook
(JNIEnv* env, jobject, jint jid, jstring jpath, jobject jmap, jobjectArray jsheets_to_load, jboolean jfilter_sheets)
{
    // Поиск листов для загрузки
    std::set<std::string> sheets_to_load;
    if (jfilter_sheets == JNI_TRUE)
    {
        jsize jsheets_to_load_len = env->GetArrayLength(jsheets_to_load);
        for (int i = 0; i < jsheets_to_load_len; ++i)
        {
            jstring jsheet_name = (jstring)(env->GetObjectArrayElement(jsheets_to_load, i));
            const char* sheet_name_chars = env->GetStringUTFChars(jsheet_name, 0);
            std::string sheet_name = sheet_name_chars;
            env->ReleaseStringUTFChars(jsheet_name, sheet_name_chars);
            sheets_to_load.insert(sheet_name);
        }
    }
    env->DeleteLocalRef(jsheets_to_load);

    // Загрузка данных
    const char* path = env->GetStringUTFChars(jpath, 0);

    env->ReleaseStringUTFChars(jpath, path);
    env->DeleteLocalRef(jpath);

    std::map<std::string, std::vector<std::vector<std::string>>> sheet_map;

    xlnt::workbook* book = new xlnt::workbook();
    book->load(path);

    for (int i = 0; i < book->sheet_count(); i++)
    {
        auto sheet = book->sheet_by_index(i);
        std::string sheet_name = sheet.title();
        if (jfilter_sheets == JNI_TRUE && !sheets_to_load.count(sheet_name))
        {
            continue;
        }

        std::vector<std::vector<std::string>> sheet_rows;
        for (auto row : sheet.rows(false))
        {
            std::vector<std::string> row_cells;
            for (auto cell : row)
            {
                row_cells.push_back(cell.to_string());
            }
            sheet_rows.push_back(row_cells);
        }
        sheet_map[sheet_name] = sheet_rows;
    }
    sheets_to_load.clear();

    if (jid == -1)
    {
        close_book(book);
    }
    else
    {
        loaded_books[jid] = book;
    }

    // C -> Java

    jclass jmap_class = env->FindClass("java/util/Map");
    jmethodID jmap_put = env->GetMethodID(jmap_class, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
    env->DeleteLocalRef(jmap_class);

    jclass jstring_array_class = env->FindClass("[Ljava/lang/String;");
    jclass jstring_class = env->FindClass("java/lang/String");

    for (auto sheet : sheet_map)
    {
        auto rows_array = sheet.second;
        jobjectArray jrows_array = env->NewObjectArray(rows_array.size(), jstring_array_class, nullptr);

        size_t rows_array_size = rows_array.size();
        for (int i = 0; i < rows_array_size; i++)
        {
            auto row = rows_array[i];
            size_t row_size = row.size();
            jobjectArray jcell_array = env->NewObjectArray(row_size, jstring_class, nullptr);
            for (int j = 0; j < row_size; j++)
            {
                jstring jval = env->NewStringUTF(row[j].c_str());
                env->SetObjectArrayElement(jcell_array, j, jval);
                env->DeleteLocalRef(jval);
            }
            env->SetObjectArrayElement(jrows_array, i, jcell_array);
            env->DeleteLocalRef(jcell_array);

            row.clear();
        }

        env->CallObjectMethod(jmap, jmap_put,
            env->NewStringUTF(sheet.first.c_str()),
            jrows_array);
        env->DeleteLocalRef(jrows_array);

        rows_array.clear();

        sheet_map.erase(sheet.first);
    }
    sheet_map.clear();

    env->DeleteLocalRef(jstring_array_class);
    env->DeleteLocalRef(jstring_class);
    env->DeleteLocalRef(jmap);

    return true;
}

JNIEXPORT jboolean JNICALL Java_ru_mkr_dbw_modules_NativeExcelModule__1writeCell
(JNIEnv* env, jobject, jint jid, jstring jsheet_name, jint jrow_id, jint jcell_id, jstring jvalue)
{
    xlnt::workbook *book = loaded_books[jid];

    // книга копируется из мэпа при записи в переменную
    // поэтому значения не сохраняются
    // <s>todo: использовать указатели вместо объектов</s>

    const char* sheet_name = env->GetStringUTFChars(jsheet_name, 0);
    auto sheet = book->sheet_by_title(sheet_name);
    env->ReleaseStringUTFChars(jsheet_name, sheet_name);

    const char* value = env->GetStringUTFChars(jvalue, 0);
    sheet.rows(false)[jrow_id][jcell_id].value(value);
    env->ReleaseStringUTFChars(jvalue, value);
    return true;
}

JNIEXPORT jboolean JNICALL Java_ru_mkr_dbw_modules_NativeExcelModule__1saveBook
(JNIEnv* env, jobject, jint jid, jstring jtarget_path)
{
    xlnt::workbook *book = loaded_books[jid];
    const char* target_path = env->GetStringUTFChars(jtarget_path, 0);
    book->save(target_path);
    env->ReleaseStringUTFChars(jtarget_path, target_path);
    return true;
}

JNIEXPORT jboolean JNICALL Java_ru_mkr_dbw_modules_NativeExcelModule__1closeBook
(JNIEnv*, jobject, jint jid)
{
    close_book(loaded_books[jid]);
    loaded_books.erase(jid);
    return true;
}

JNIEXPORT jboolean JNICALL Java_ru_mkr_dbw_modules_NativeExcelModule__1dispose
(JNIEnv*, jobject)
{
    for (auto entry : loaded_books)
    {
        close_book(entry.second);
        loaded_books.erase(entry.first);
    }
    loaded_books.clear();
    return true;
}