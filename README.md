# Tomatology

An Android application that identifies ten tomato leaf diseases from a
photograph, using a convolutional neural network trained for the purpose and
delivered to the phone over the air rather than bundled into the APK.

Built as my master's thesis. Kotlin, TensorFlow Lite, Firebase.

---

## What it does

Point the camera at a tomato leaf, or pick a photo from the gallery. The model
runs on the device and returns a confidence for each of the ten classes. The
app shows the top three, expandable to a short summary of the symptoms for each,
so the answer is presented as a shortlist to judge rather than a single verdict
to trust.

You pick the one that matches. That choice does two things: it opens the full
entry for that disease, with symptoms, causes, treatment and prevention, and it
saves the result to your archive with the photo and the date. Previous results
are browsable and reopen the same detail page.

The ten classes are bacterial spot, early blight, healthy, late blight, leaf
mold, mosaic virus, septoria leaf spot, target spot, two spotted spider mite and
yellow leaf curl virus.

---

## The model

Trained on **45,860 labelled leaf images** across the ten classes.

The work that made the difference was the preprocessing rather than the
architecture. A pipeline in Python and OpenCV removes the background and
isolates the leaf: k-means segmentation, a grayscale and histogram pass to drop
the background, Gaussian smoothing, then Sobel, non-maximum suppression and
dilation to keep the lesion edges. That pipeline took accuracy from **82% to
roughly 98%**.

I benchmarked 28 off-the-shelf Keras architectures against the custom network on
the same data. None of them beat it.

The trained model is 139 MB, which is far too large to ship to a phone. Post
training quantisation brings it to **11 MB for about two points of accuracy**,
98% down to roughly 96%. That trade is the right one here: a model nobody
downloads has an effective accuracy of zero.

---

## How the model gets to the phone

This is the part of the project I would point at first.

The model is not in the APK. On launch the app reads a model name from
**Firebase Remote Config**, then asks **Firebase ML** to download that model,
over wifi only. The TensorFlow Lite interpreter is built from the downloaded
file.

Three things follow from that:

**A new model ships without an app release.** Change the Remote Config value,
and the next launch picks up the new model. No store review, no update prompt,
no users stranded on an old version.

**Models can be A/B tested in production.** Remote Config can serve different
model names to different slices of the user base, so two models can be compared
on real photographs from real phones rather than on a held-out split of the
training set.

**The input size is read from the model, not hardcoded.** The interpreter reads
the input tensor shape at initialisation, so a model trained at a different
resolution drops in without touching the app.

Measuring it is the other half. When a user picks the disease that matches,
the app logs a `correct_inference` analytics event if their choice was the
model's top prediction. That turns everyday use into a live accuracy signal on
real-world photographs, which is a different and more honest number than test
set accuracy. Firebase Performance traces wrap the model download and each
classification, so the cost of both is visible in production.

---

## Running it

You need Android Studio, a device or emulator on API 29 or higher, and your own
Firebase project. The `google-services.json` in this repository points at mine
and will not work for you.

**1. Create a Firebase project** and add an Android app with the package name
`com.example.tomatology`. Download its `google-services.json` and replace
`app/google-services.json`.

**2. Enable these Firebase products:** Authentication with the Google provider,
Realtime Database, Remote Config, ML model deployment, Analytics and
Performance Monitoring.

**3. Upload a model.** Publish one of the files in `Model/` as a custom model in
Firebase ML, then add a Remote Config parameter called `model_name` whose value
is the name you gave it.

**4. Seed the Realtime Database.** The app expects this shape:

```
guide          a single string, shown on the guide screen
information    a list of ten entries, one per class, in class id order:
                 causes, diseaseName, prevention, symptoms,
                 symptomsSummary, treatment
users/{uid}/{timestamp}   written by the app: disease, diseaseID,
                          diseasePicture, date
```

The `information` list is indexed by the model's class id, so the order matters.
Healthy is index 2 and carries no content, which the app inserts itself.

**5. Set your database rules** so that a signed in user can read `guide` and
`information` and can only read and write their own subtree under `users`.

Then build and run.

---

## Layout

```
app/src/main/java/com/example/tomatology/
  MainActivity.kt              camera, gallery, Google sign in, model download
  TomatoDiseaseClassifier.kt   the TensorFlow Lite interpreter and preprocessing
  ResultActivity.kt            top three predictions, user confirmation, archive write
  DetailsActivity.kt           symptoms, causes, treatment, prevention
  ArchiveActivity.kt           previous results for the signed in user
  GuideActivity.kt             how to photograph a leaf
  RecyclerAdapter.kt           archive rows
  ExpandableListViewAdapter.kt shared expandable list used by results and details
  Prediction.kt, Information.kt   the two data classes, both Parcelable

Model/                         the exported TensorFlow Lite models
```

---

## Since the thesis

I came back to this in 2026 and fixed things I had shipped and not noticed.

The archive was the worst of them. Every row in the list opened the same
hardcoded disease, because the adapter was handed a single `Information` object
instead of the list and the row's class id. Tapping any past result showed you
the same page. The adapter now takes the id list and looks up the right entry.

The database listener's error branch was a `TODO("Not yet implemented")`, which
throws. A database error would have crashed the archive screen rather than
logging and carrying on.

Beyond that: removed the commented-out image handling that had been left in the
archive path, and tidied the duplicated work in the result screen.

---

## What I would do next

- **Move off the deprecated APIs.** `startActivityForResult` and the permission
  callbacks predate the Activity Result APIs, and the dependency list is pinned
  to a lot of 2021 alphas and betas.
- **Store photos properly.** Archived images are base64 encoded into the
  Realtime Database. Firebase Storage is the right home for them, with the
  database holding a reference.
- **Camera photos are thumbnails.** The camera path takes the small preview
  bitmap out of the intent extras rather than writing a full resolution file,
  so the model sees less detail than it could.
- **Handle the empty archive.** The list has no empty state and no loading
  state.
- **Tests.** There are none. The classifier's output ordering and the
  index-to-disease mapping are both worth pinning, since that mapping is exactly
  what the archive bug got wrong.
