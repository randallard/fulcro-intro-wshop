(ns holyjak.fulcro-exercises
  "This is a series of exercises to help you learn Fulcro, via the REPL. See the README.md!

  How to use:
  - Load this file into the cljs REPL (using the commands provided by your editor)
  - For each exercise:
    1. Un-comment *temporarily* the exercise you are working on (by replacing `(comment ...`
      with `(do ;comment ...`) Shadow-cljs will thus evaluate the code and it will render.
      Tip: Hard-reload the page occasionally to get rid of residues of past exercises.
    2. Read the instructions in the comments, make your changes, wait for shadow to apply them
    3. Regularly look at what your solution looks like in Fulcro Inspect (Client DB, ...) to
       get familiar with this essential tool.
    4. Call `(hint <exercises number>)` to get help. Repeated calls may provide more help.
       Even if you want to do it without help, check out all the hints eventually. They may
       contain useful insights.
    5. When done, compare your solution with mine in the `holyjak.solutions` namespace.
    6. Finally, comment-out the exercise again and go on to the next one.
  - See 'Troubleshooting and getting help during the exercises' in the README
  "
  (:require
    [holyjak.fulcro-exercises.impl :refer [hint config-and-render! show-client-db]]
    [com.fulcrologic.fulcro.algorithms.merge :as merge]
    [com.fulcrologic.fulcro.algorithms.data-targeting :as targeting]
    [com.fulcrologic.fulcro.algorithms.normalized-state :as norm]
    [com.fulcrologic.fulcro.application :as app]
    [com.fulcrologic.fulcro.components :as comp :refer [defsc transact!]]
    [com.fulcrologic.fulcro.data-fetch :as df]
    [com.fulcrologic.fulcro.mutations :refer [defmutation]]
    [com.fulcrologic.fulcro.dom :as dom :refer [button div form h1 h2 h3 input label li ol p ul]]
    [com.wsscode.pathom.connect :as pc :refer [defresolver]]
    [com.fulcrologic.fulcro.mutations :as m]))

(defn init [])

(do ;comment ; 8 Fix the graph
  (do
    ;; TASK:
    ;; Fix the code to actually show the list of cities.
    ;;
    ;; LEARNING OBJECTIVES: 
    ;; - Understand the importance of connections in the frontend data and how 
    ;;   to establish them
    ;; - Understand that you can add any arbitrary connection between components;
    ;;   they do not need to come only from the backend data
    ;;
    ;; RESOURCES:
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/index.html#_components_initial_state
    ;; - https://fulcro-community.github.io/guides/tutorial-minimalist-fulcro/index.html#_components_query
    ;;
    ;; TIP: You need to fix 4 places, most of them in Root8
    ;;
    (defsc Menu [this {:keys [cities selected-city]}]
      {:ident (fn [] [:component/id ::Menu])
       :query [:cities :selected-city]
       :initial-state {}}
      ;; Note: This is not a very good way of using a select :-) 
      (dom/select {:value (or selected-city "Select one:")
                   :onChange #(do (println "Selected city:" (.-value (.-target %)))
                                (m/set-string! this :selected-city :event %))}
        (->> (cons "Select one:" cities)
             (mapv #(dom/option {:key %, :value %} %)))))

    (def ui-menu (comp/factory Menu))

    (defsc Root8 [this {menu :menu :as props}]
      {:query [{:menu (comp/get-query Menu)}]
       :initial-state {:menu {}}}
      (dom/div
        (h1 "Select a city!")
        (ui-menu menu)))
    
    (defresolver cities [_ _]
      {::pc/input #{}
       ::pc/output [:cities]}
      {:cities ["Linköping" "Oslo" "Prague"]})

    ;; Render the app, with a backend using these resolvers
    (def app8 (config-and-render! Root8 {:resolvers [cities]}))

    ;; We want to load :cities into the Menu component because it is the only
    ;; one needing the data (rather then polluting root with it and having to
    ;; use a Link Query)
    (df/load! app8 :cities nil {:target 
                                (conj (comp/get-ident Menu {}) :cities)
                                })
    ))

;; TODO Additional exercises:
;; - computed props for passing a callback from the parent
;; - create/delete/create+delete <> tmpids; simulated failure => undo the optimistic change?
;; - link queries & more from https://blog.jakubholy.net/2020/fulcro-divergent-ui-data/
;; - anything else from the Minim. Fulcro Tutorial should be added?
;;   - Initial state propagation?
;;   - Computed props for a callback or parent-visible prop
;;   - pre-merge ?! / loading dyn. data for a defsc containing a router x we did not learn routers
;;   - Link Query? But not covered by MFT
;; Other:
;; @peterdee:  incremental loading of big trees of data. Incremental loading is probably discussed 
;; adequately in the Developer Guide, but not with recursive queries, and incremental expansion of a UI tree, 
;; I think. If that seems like too much an edge case, maybe something simpler with trees.
