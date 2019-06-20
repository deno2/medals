(ns medals.widget
  (:require
   [reagent.core :as r]
   [ajax.core :as ajax]))


;; Config
;;;;;;;;;;;;;;;;;;;;;;;

; (def config {:data-uri "https://s3-us-west-2.amazonaws.com/reuters.medals-widget/medals.json"
;              :flags-uri "https://s3-us-west-2.amazonaws.com/reuters.medals-widget/flags.png"})

(def config {:data-uri "data/medals.json"
             :flags-uri "images/flags.png"})


;; State
;;;;;;;;;;;;;;;;;;;;;;;
(defn init-state [sort-key]
  (r/atom {:medals nil
           :sort-key sort-key
           :widget-state :ready}))

(defn sort-key [s]
  (let [key (-> s
                name
                clojure.string/trim
                clojure.string/lower-case
                keyword)]
    (if (#{:gold :silver :bronze :total} key)
      key
      :gold)))

(def sort-table {:gold [:gold :silver]
                 :total [:total :gold]
                 :silver [:silver :gold]
                 :bronze [:bronze :gold]})

(defn sort-medals [medals sort-key]
  (let [sort-fn (apply juxt (get sort-table sort-key))
        cmp-fn (fn [a b]
                 (compare b a))]
    ;; Note: Using the custom compare fn to reverse the order here
    ;; because using > sorts by alpabetical order s-)
    (sort-by sort-fn cmp-fn medals)))


(defn assoc-totals [medals]
  (map (fn [medal]
         (assoc medal :total (+ (:gold medal)
                                (:silver medal)
                                (:bronze medal))))
       medals))

(defn process-medals! [state sort-key]
  (println "Process Medals")
  (let [sorted-medals (sort-medals (:raw-data @state) sort-key)]
    (swap! state assoc :medals (take 10 (map-indexed (fn [idx medal]
                                                       (assoc medal
                                                              :index (inc idx)))
                                                     sorted-medals))
           :sort-key sort-key)))

(defn load-data! [state]
  (println "Loading Medals")
  (swap! state assoc :loading? true :error? false)
  (ajax/GET
    (:data-uri config)
    {:handler  (fn [medals]
                 (.log js/console "Loaded Medals" medals)
                 (swap! state assoc :raw-data (assoc-totals medals))
                 (process-medals! state (:sort-key @state))
                 (swap! state assoc :loading? false))
     :error-handler (fn [{:keys [status status-text]}]
                      (.log js/console (str "Error: " status " " status-text))
                      (swap! state assoc :error? true :loading? false))
     :response-format :json
     :keywords? true}))


;; Views
;;;;;;;;;;;;;;;;;;;;;;;
(defn loading-message []
  [:div "LOADING"])

(defn error-message []
  [:div "There was an error loading data from the server. Please try again later."])


(defn flag [{:keys [code] :as medal}]
  [:div.flag {:class (clojure.string/lower-case code)}])

(defn medal-row [{:keys [index code gold silver bronze total] :as medal}]
  (println "Medal Row")
  [:tr
   [:td index]
   [:td [flag medal]]
   [:td code]
   [:td gold]
   [:td silver]
   [:td bronze]
   [:td total]])


(defn medal-table [state]
  (println "Medal Table sorted by: " (:sort-key @state))
  (let [sorted-by? (fn [k] (when (= k (:sort-key @state))
                             {:class "sorted"}))]
    (if (:loading? @state)
      [loading-message]
      [:table.medals-table
       [:thead
        [:tr.medals-header-row
         [:th {:col-span 3}]
         [:th.sortable (merge {:on-click (fn [e] (process-medals! state :gold))
                               :title "Sort by Gold"}
                              (sorted-by? :gold)) [:div.circle.gold]]
         [:th.sortable (merge {:on-click (fn [e] (process-medals! state :silver))
                               :title "Sort by Silver"}
                              (sorted-by? :silver)) [:div.circle.silver]]
         [:th.sortable (merge {:on-click (fn [e] (process-medals! state :bronze))
                               :title "Sort by Bronze"}
                              (sorted-by? :bronze)) [:div.circle.bronze]]
         [:th.sortable (merge {:on-click (fn [e] (process-medals! state :total))
                               :title "Sort by Total"}
                              (sorted-by? :total)) [:div.total "Total"]]]]
       [:tbody
        (for [medal (:medals @state)]
          ^{:key (:index medal)} [medal-row medal])]])))



(defn medals
  "Renders a table of Olympic Medals sorted by either gold, silver, bronze 
or total medals. Clicking a header will sort by that column."
  ([]
   (medals "gold"))
  ([sort]
   (let [state (init-state (sort-key sort))
         _ (load-data! state)]
     (fn render-fn [sort]
       [:div.medals-widget
        [:h3.medals-header "Medal Count"]
        [medal-table state]]))))
