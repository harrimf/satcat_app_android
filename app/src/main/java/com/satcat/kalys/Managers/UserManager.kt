package com.satcat.kalys.Managers

import com.satcat.kalys.Models.User
import io.realm.Realm
import io.realm.kotlin.where

open class UserManager  {

    companion object {
        @JvmStatic
        val shared = UserManager()
    }

    fun getUser(): User {
        val realm: Realm = Realm.getDefaultInstance()
        val localUser = realm.where<User>().equalTo("IsLocal", true).findFirst()
        return localUser!!
    }

    fun setLocalUser(id: String) {
        removeLocalUsers()

        val realm: Realm = Realm.getDefaultInstance()

        val user = realm.where<User>().equalTo("ID", id).findFirst()

        realm.executeTransaction {
            user!!.IsLocal = true
        }

    }

    fun removeLocalUsers() {
        val realm: Realm = Realm.getDefaultInstance()

        val localUsers = realm.where<User>().equalTo("IsLocal", true).findAll().toList()

        if(localUsers.count() > 0) {
            for (user in localUsers) {
                realm.executeTransaction {
                    user.IsLocal = false
                }
            }
        }

    }

    fun isOneLocalUser(): Boolean {
        val realm: Realm = Realm.getDefaultInstance()

        val localUsers = realm.where<User>().equalTo("IsLocal", true).findAll().toList()

        return (localUsers.count() == 1)

    }

}