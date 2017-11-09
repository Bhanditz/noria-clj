(ns noria.tests
  (:require [noria :refer :all]
            [clojure.test :refer :all]
            [noria.components :refer :all]))

(defattr :dom/children {:noria/data-type :nodes-seq})
(defattr :dom/child {:noria/data-type :node})

(defn check-updates [elements]
  (reduce (fn [[c-id ctx ups] [el updates]]
            (let [[c-id' ctx'] (reconcile c-id el ctx)]
              (is (= updates (:updates ctx')) "wrong updates")
              [c-id' (assoc ctx' :updates []) (:updates ctx')]))
          [nil context-0 []] elements))


(deftest reconcile-seq
  (check-updates
   [[{:noria/type :div
      :dom/children
      [{:noria/type :div
        :noria/key :hey
        :dom/text "hey"}
       {:noria/type :div
        :noria/key :hoy
        :dom/text "hoy"}]}
     [#:noria{:update-type :make-node, :node 0, :type :div, :constructor-parameters {}}
      #:noria{:update-type :make-node, :node 1, :type :div, :constructor-parameters {}}
      #:noria{:update-type :set-attr, :attr :dom/text, :node 1, :value "hey"}
      #:noria{:update-type :make-node, :node 2, :type :div, :constructor-parameters {}}
      #:noria{:update-type :set-attr, :attr :dom/text, :node 2, :value "hoy"}
      #:noria{:update-type :add, :attr :dom/children, :node 0, :value 1, :index 0}
      #:noria{:update-type :add, :attr :dom/children, :node 0, :value 2, :index 1}]]
    [{:noria/type :div
      :dom/children
      [{:noria/type :div
        :noria/key :hey
        :dom/text "hey"}
       {:noria/type :div
        :noria/key :hoy
        :dom/text "hoy"}]} []]
    [{:noria/type :div
      :dom/children [{:noria/type :div
                      :noria/key :hiy
                      :dom/text "hiy"}
                     {:noria/type :div
                      :noria/key :hoy
                      :dom/text "hoy!!"}
                     {:noria/type :div
                      :noria/key :fu
                      :dom/text "fu"}]}
     [#:noria{:update-type :make-node, :node 3, :type :div, :constructor-parameters {}}
      #:noria{:update-type :set-attr, :attr :dom/text, :node 3, :value "hiy"}
      #:noria{:update-type :set-attr, :attr :dom/text, :node 2, :value "hoy!!"}
      #:noria{:update-type :make-node, :node 4, :type :div, :constructor-parameters {}}
      #:noria{:update-type :set-attr, :attr :dom/text, :node 4, :value "fu"}
      #:noria{:update-type :add, :attr :dom/children, :node 0, :value 3, :index 0}
      #:noria{:update-type :add, :attr :dom/children, :node 0, :value 4, :index 2}
      #:noria{:update-type :destroy, :node 1}]]
    [{:noria/type :div
      :dom/children [{:noria/type :div
                      :noria/key :hoy
                      :dom/text "hoy!!"}
                     {:noria/type :div
                      :noria/key :hiy
                      :dom/text "hiy"}
                     {:noria/type :div
                      :noria/key :fu
                      :dom/text "fu"}]}
     [#:noria{:update-type :remove, :attr :dom/children, :node 0, :value 2}
      #:noria{:update-type :add, :attr :dom/children, :node 0, :value 2, :index 0}]]]))

(def label
  (render
   (fn [x]
     {:noria/type :div
      :noria/text (str x)})))

(def lambda
  (render
   (fn [y]
     ['apply (fn [x]
               {:noria/type :div
                :dom/child x
                :dom/children [x]})
      [label y]])))

(deftest reconcile-lambda
  (check-updates [[[lambda "hello"]
                   [#:noria{:update-type :make-node, :node 0, :type :div, :constructor-parameters {}}
                    #:noria{:update-type :set-attr, :attr :noria/text, :node 0, :value "hello"}
                    #:noria{:update-type :make-node, :node 1, :type :div, :constructor-parameters {}}
                    #:noria{:update-type :set-attr, :attr :dom/child, :node 1, :value 0}
                    #:noria{:update-type :add, :attr :dom/children, :node 1, :value 0, :index 0}]]
                  [[lambda "hello"] []]
                  [[lambda "bye"]
                   [#:noria{:update-type :set-attr, :attr :noria/text, :node 0, :value "bye"}]]]) 
  )

(def simple-container
  (render
   (fn [i]
     {:noria/type :div
      :dom/children (map (fn [i]
                           ^{:noria/key i} [label i]) (range i))})))

(deftest reconcile-simple-list
  (check-updates [[[simple-container 2]
                   [#:noria{:update-type :make-node, :node 0, :type :div, :constructor-parameters {}}
                    #:noria{:update-type :make-node, :node 1, :type :div, :constructor-parameters {}}
                    #:noria{:update-type :set-attr, :attr :noria/text, :node 1, :value "0"}
                    #:noria{:update-type :make-node, :node 2, :type :div, :constructor-parameters {}}
                    #:noria{:update-type :set-attr, :attr :noria/text, :node 2, :value "1"}
                    #:noria{:update-type :add, :attr :dom/children, :node 0, :value 1, :index 0}
                    #:noria{:update-type :add, :attr :dom/children, :node 0, :value 2, :index 1}]]
                  [[simple-container 2]
                   []]
                  [[simple-container 1]
                   [#:noria{:update-type :destroy, :node 2}]]
                  [[simple-container 3]
                   [#:noria{:update-type :make-node, :node 3, :type :div, :constructor-parameters {}}
                    #:noria{:update-type :set-attr, :attr :noria/text, :node 3, :value "1"}
                    #:noria{:update-type :make-node, :node 4, :type :div, :constructor-parameters {}}
                    #:noria{:update-type :set-attr, :attr :noria/text, :node 4, :value "2"}
                    #:noria{:update-type :add, :attr :dom/children, :node 0, :value 3, :index 1}
                    #:noria{:update-type :add, :attr :dom/children, :node 0, :value 4, :index 2}]]
                  [[simple-container 2]
                   [#:noria{:update-type :destroy, :node 4}]]])
  )

(def do-block
  (render
   (fn [i]
     (into ['do] (map (fn [i] [label i])) (range i)))))

(deftest reconcile-do-block
  (check-updates [[[do-block 3]
                   [#:noria{:update-type :make-node, :node 0, :type :div, :constructor-parameters {}}
                    #:noria{:update-type :set-attr, :attr :noria/text, :node 0, :value "0"}
                    #:noria{:update-type :make-node, :node 1, :type :div, :constructor-parameters {}}
                    #:noria{:update-type :set-attr, :attr :noria/text, :node 1, :value "1"}
                    #:noria{:update-type :make-node, :node 2, :type :div, :constructor-parameters {}}
                    #:noria{:update-type :set-attr, :attr :noria/text, :node 2, :value "2"}]]
                  [[do-block 3] []]
                  [[do-block 1]
                   [#:noria{:update-type :destroy, :node 1}
                    #:noria{:update-type :destroy, :node 2}]]])
  )

(noria/defattr :NSWindow/contentView {:noria/data-type :node})

(def text-field
  (render
   (fn []
     {:noria/type :NSTextField
      :NSTextField/stringValue "hello"})))

(def window
  (render
   (fn []
     {:noria/type :NSWindow
      :NSWindow/contentView [text-field]})))

(defconstructor :fake/constraint #{:constraint/view1
                                   :constraint/view2})
(defconstructor :fake/label #{:label/text})
(defconstructor :fake/label2 #{:label2/text})

(def constraint
  (render
   (fn [text1 text2]
     ['apply
      (fn [view1 view2]
        ['do
          {:noria/type :fake/constraint
           :constraint/view1 view1
           :constraint/view2 view2}
          {:noria/type :Container
           :dom/children [view1 view2]}])
      {:noria/type :fake/label
       :label/text text1}
      {:noria/type :fake/label2
       :label2/text text2}])))

(deftest constructor
  (check-updates [[[constraint "hey" "hoy"]
                   [#:noria{:update-type :make-node,
                            :node 0,
                            :type :fake/label,
                            :constructor-parameters {:label/text "hey"}}
                    #:noria{:update-type :make-node,
                            :node 1,
                            :type :fake/label2,
                            :constructor-parameters {:label2/text "hoy"}}
                    #:noria{:update-type :make-node,
                            :node 2,
                            :type :fake/constraint
                            :constructor-parameters #:constraint{:view1 2, :view2 3}}
                    #:noria{:update-type :make-node,
                            :node 3,
                            :type :Container,
                            :constructor-parameters {}}
                    #:noria{:update-type :add,
                            :attr :dom/children,
                            :node 3,
                            :value 0,
                            :index 0}
                    #:noria{:update-type :add,
                            :attr :dom/children,
                            :node 3,
                            :value 1,
                            :index 1}]]
                  [[constraint "he" "ho"]
                   [#:noria{:update-type :set-attr,
                            :attr :label/text,
                            :node 0,
                            :value "he"}
                    #:noria{:update-type :set-attr,
                            :attr :label2/text,
                            :node 1, :value "ho"}]]]))

(deftest reconcile-node-attr
  (check-updates [[[window]
                   [#:noria{:update-type :make-node,
                            :node 0,
                            :type :NSWindow,
                            :constructor-parameters {}}
                    #:noria{:update-type :make-node,
                            :node 1,
                            :type :NSTextField,
                            :constructor-parameters {}}
                    #:noria{:update-type :set-attr,
                            :attr :NSTextField/stringValue,
                            :node 1,
                            :value "hello"}
                    #:noria{:update-type :set-attr,
                            :attr :NSWindow/contentView,
                            :node 0,
                            :value 1}]]]))


(run-tests)

(comment

  (defconstructor :NSView #{:NSView/frame})

  {:noria/type :NSView
   :NSView/frame {:CGFrame/origin {:CGPoint/x 10
                                   :CGPoint/y 10}
                  :CGFrame/size {:CGSize/width 100
                                 :CGSize/height 100}}
   :NSView/subviews []}

  

  )
