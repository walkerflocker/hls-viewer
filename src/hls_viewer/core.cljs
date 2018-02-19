(ns hls-viewer.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require
   [hls-viewer.reqs :as reqs]
   [hls-viewer.urls :as urlUtil]
   [cljs.core.async :refer [<!]]
   [rum.core :as rum]))

(enable-console-print!)

(defn setManifestText [url textAtom]
  (go (let [manifest (<! (reqs/getManifest url))]
        (reset! textAtom manifest))))

(defn onLineClick [line urlAtom textAtom]
  (let [destUrl  (urlUtil/constructUrl @urlAtom line)]
    (if (re-matches #".+\.m3u8" destUrl)
      (setManifestText destUrl textAtom)
      (reqs/downloadUrl destUrl))))

(rum/defc displayContainer < rum/reactive
  [displayText url]
  [:div {:style {:font-size "12px" :padding "20px"}}
   (for [line (rum/react displayText)]
     [:div [:span
            (if (re-matches #".+\.(m3u8|ts|vtt)" line)
              {:on-click #(onLineClick line url displayText)
               :style {
                       :color "blue"
                       :cursor "pointer"
                     }}) line]])])

(rum/defc headerContainer [url displayText]
  [:div {:style {:text-align "center" :padding "10px"}}
   [:input {:style {:border 0
                    :border-bottom "1px solid #C0DEC5"
                    :outline "none"
                    :width "400px"
                    :background-color "inherit"
                    :color "inherit"
                    }
            :placeholder "Enter Manifest"
            :on-change #(reset! url (.. % -target -value))
            :on-key-up #(if (= "Enter" (.. % -key))
                           (setManifestText @url displayText))
            }]])

(rum/defc wrapper []
  (let [url (atom "")
        displayText (atom [])]
    [(headerContainer url displayText)
    (displayContainer displayText url)]))

(defn init [] (rum/mount (wrapper)
              (.getElementById js/document "app")))
(defn on-js-reload [] init)(init)
