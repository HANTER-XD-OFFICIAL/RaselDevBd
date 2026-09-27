# Rasel Dev BD — GitHub Pages Hosting Guide (বাংলা ও English)

আপনার **Rasel Dev BD** ওয়েবসাইটটি সরাসরি **GitHub Pages**-এ হোস্ট করার জন্য সব ফাইল তৈরি করে দেওয়া হয়েছে। আপনি নিচের যেকোনো একটি পদ্ধতিতে ১ মিনিটে সাইটটি লাইভ করতে পারবেন:

---

## পদ্ধতি ১: কোনো Build ছাড়াই সরাসরি `/docs` ফোল্ডার থেকে হোস্ট (সবচেয়ে সহজ)

এই প্রজেক্টের `/docs` ফোল্ডারে সম্পূর্ণ স্ট্যাটিক ওয়েবসাইট (`index.html`, `404.html`, `.nojekyll`, এবং `data/content-bundle.json`) তৈরি করা আছে।

1. আপনার কোডটি GitHub Repository-তে Push করুন।
2. GitHub Repository-এর **Settings** → **Pages**-এ যান।
3. **Build and deployment** সেকশনে:
   - **Source**: `Deploy from a branch` সিলেক্ট করুন।
   - **Branch**: `main` এবং ফোল্ডার হিসেবে `/docs` সিলেক্ট করে **Save** চাপুন।
4. ৩০ সেকেন্ডের মধ্যে আপনার ওয়েবসাইট লাইভ হয়ে যাবে!
   - **Admin Dashboard PIN**: `2026`

---

## পদ্ধতি ২: GitHub Actions (Vite + React Build) ব্যবহার করে হোস্ট

এই প্রজেক্টে `/github-pages-site/` ফোল্ডারে সম্পূর্ণ Vite + React প্রজেক্ট এবং `/.github/workflows/deploy-gh-pages.yml` ফাইল যুক্ত করা আছে।

1. GitHub Repository-এর **Settings** → **Pages**-এ যান।
2. **Source** হিসেবে `GitHub Actions` সিলেক্ট করুন।
3. `main` ব্রাঞ্চে Push করলেই স্বয়ংক্রিয়ভাবে Vite React প্রজেক্টটি Build হয়ে GitHub Pages-এ Deploy হয়ে যাবে!

---

## পদ্ধতি ৩: Android অ্যাপের ভেতর থেকে ১-ক্লিকে `.zip` ডাউনলোড

আপনি **Rasel Dev BD** Android অ্যাপের ভেতরে **Admin** ট্যাবে গিয়ে (PIN: `2026`) **GitHub Pages SSG** সেকশন থেকে **"Download Complete GitHub Pages ZIP"** বাটনে ট্যাপ করলে আপনার নতুন সব ব্লগ, ছবি, ও প্রজেক্টসহ একটি রেডিমেড `rasel-dev-bd-gh-pages.zip` ফাইল সরাসরি ডাউনলোড হয়ে যাবে।
