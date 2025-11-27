package com.liufeng.ballfight


class PauseMenuDialog : DialogFragment() {
    private lateinit var binding: DialogPauseBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, 
                             savedInstanceState: Bundle?): View {
        binding = DialogPauseBinding.inflate(inflater)
        setupProfileSection()
        setupButtons()
        return binding.root
    }

    private fun setupProfileSection() {
        // 用户档案显示
        binding.tvUid.text = getString(R.string.uid_format, UserProfile.uid)
        binding.tvNickname.text = UserProfile.nickname
        binding.tvGender.text = UserProfile.genderSymbol
        
        // 个性签名样式
        binding.tvSignature.apply {
            text = if (UserProfile.signature.isEmpty()) {
                setTextColor(ContextCompat.getColor(context, R.color.text_hint))
                resources.getString(R.string.default_signature)
            } else {
                setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                UserProfile.signature
            }
        }
    }

    private fun setupButtons() {
        binding.btnEditProfile.setOnClickListener {
            ProfileEditorDialog().show(parentFragmentManager, "profile_editor")
        }
        
        binding.btnLeaderboard.setOnClickListener {
            showLeaderboard()
        }
    }

    private fun showLeaderboard() {
        // 排行榜实现
    }
}