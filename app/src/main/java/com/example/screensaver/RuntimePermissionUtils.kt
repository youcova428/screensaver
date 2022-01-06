package com.example.screensaver

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentManager

class RuntimePermissionUtils {

    fun showAlertDialog(fragmentManager: FragmentManager, permission: String) {
        val dialog = RuntimePermissionAlertDialogFragment.newInstance(permission)
        dialog.show(fragmentManager, RuntimePermissionAlertDialogFragment.TAG)
    }

    class RuntimePermissionAlertDialogFragment : DialogFragment() {

        companion object {
            const val TAG = "RuntimePermissionApplicationSettingsDialogFragment"
            private const val ARG_PERMISSION_NAME = "permissionName"
            fun newInstance(permission: String):
                    RuntimePermissionAlertDialogFragment {
                val fragment = RuntimePermissionAlertDialogFragment()
                val args = Bundle()
                args.putString(ARG_PERMISSION_NAME, permission)
                fragment.arguments = args
                return fragment
            }
        }

        override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {

            val permission = arguments?.getString(ARG_PERMISSION_NAME)
            val dialogBuilder = AlertDialog.Builder(activity)
                .setMessage(permission!! + "の権限がないので、アプリ情報の「許可」から設定してください")
                .setPositiveButton("アプリ情報", DialogInterface.OnClickListener { _, _ ->
                    dismiss()
                    val intent = Intent(
                        android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                        Uri.parse("package:" + activity?.packageName)
                    )
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    activity?.startActivity(intent)
                })
                .setNegativeButton("キャンセル", DialogInterface.OnClickListener { _, _ -> dismiss() })
            return dialogBuilder.create()
        }
    }

}